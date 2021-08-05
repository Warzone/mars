package network.warzone.pgm.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.events.ApiConnectedEvent
import network.warzone.pgm.api.socket.InboundEvent
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.exceptions.MissingConfigPathException
import network.warzone.pgm.utils.zlibCompress
import network.warzone.pgm.utils.zlibDecompress
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

@Serializable
data class Packet<T>(
    @SerialName("e") val event: String,
    @SerialName("d") val data: T,
)

@Serializable
data class AuthData(val id: String, val secret: String)

class ApiClient(serverId: String, config: ConfigurationSection) {

    companion object {
        val API_SCOPE = CoroutineScope(SupervisorJob())
    }

    val client: HttpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(WebSockets)

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    lateinit var httpUrl: String
    private lateinit var websocketSession: WebSocketSession

    init {
        loadHttp(config)
        loadSocket(serverId, config)
    }

    suspend inline fun <reified T> get(url: String): T {
        return client.get(httpUrl + url)
    }

    suspend inline fun <reified T, K> post(url: String, body: K): T {
        return client.post(httpUrl + url) {
            this.body = body!!
        }
    }

    suspend inline fun <reified T, K> put(url: String, body: K): T {
        return client.put(httpUrl + url)
    }

    suspend inline fun <reified T> delete(url: String): T {
        return client.delete(httpUrl + url)
    }

    fun <T> emit(type: OutboundEvent<T>, data: T) {
        val jsonString = Json.encodeToString(Packet(type.eventName, data))

        API_SCOPE.launch {
            websocketSession.send(Frame.Binary(true, jsonString.zlibCompress()))
        }
    }

    private fun loadHttp(config: ConfigurationSection) {
        val httpConfig = config.getConfigurationSection("http") ?: throw MissingConfigPathException("api.http")

        httpUrl = httpConfig.getString("url") ?: throw MissingConfigPathException("api.http.url")
        //TODO: auth -> set default header.
    }

    private fun loadSocket(serverId: String, config: ConfigurationSection) {
        val socketConfig = config.getConfigurationSection("socket") ?: throw MissingConfigPathException("api.socket")

        val socketUrl = socketConfig.getString("url") ?: throw MissingConfigPathException("api.socket.url")
        val socketSecret = socketConfig.getString("secret") ?: throw MissingConfigPathException("api.socket.secret")

        API_SCOPE.launch {
            createSocket(socketUrl, serverId, socketSecret)
        }
    }

    private fun createSocket(url: String, serverId: String, secret: String) = runBlocking {
        client.ws(urlString = "$url/minecraft") {
            websocketSession = this

            emit(OutboundEvent.IDENTIFY, AuthData(serverId, secret))

            Bukkit.getPluginManager().callEvent(ApiConnectedEvent(WarzonePGM.get().apiClient))

            while (true) {
                val optionalFrame = incoming.tryReceive()

                if (optionalFrame.isSuccess) {
                    val frame = optionalFrame.getOrNull()

                    println("Got frame: $frame")
                    frame as? Frame.Binary ?: continue

                    val jsonPacket = frame.data.zlibDecompress()
                    println("Received $jsonPacket")

                    val jsonElement = Json.parseToJsonElement(jsonPacket).jsonObject
                    val eventName = jsonElement["e"]?.jsonPrimitive?.content

                    val event = InboundEvent.valueOf<Any>(eventName!!)

                    jsonElement["d"]?.let { event.call(it) }
                }
            }
        }
    }

}