package network.warzone.pgm.api

import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.tinder.streamadapter.coroutines.CoroutinesStreamAdapterFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import network.warzone.pgm.api.events.ApiConnectedEvent
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.api.socket.WarzoneService
import network.warzone.pgm.utils.*
import okhttp3.OkHttpClient
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import java.util.logging.Level

data class Packet<T>(
    @SerializedName("e") val event: String,
    @SerializedName("d") val data: T,
)

class ApiClient(private val serverId: String, private val config: ConfigurationSection) {

    private val logger = createLogger(this::class)
    val client: HttpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                registerTypeAdapter(Date::class.java, JsonDeserializer {
                    json, _, _ -> Date(json.asJsonPrimitive.asLong)
                })
                registerTypeAdapter(Date::class.java, JsonSerializer<Date> {
                        date, _, _ -> JsonPrimitive(date.time)
                })
            }
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    lateinit var socket: WarzoneService
    lateinit var httpUrl: String

    init {
        logger.level = Level.ALL
    }

    suspend inline fun <reified T> get(url: String): T {
        return client.get(httpUrl + url)
    }

    suspend inline fun <reified T> post(url: String): T {
        return client.post(httpUrl + url)
    }

    suspend inline fun <reified T, K : Any> post(url: String, body: K): T {
        return client.post(httpUrl + url) {
            this.body = body
        }
    }

    suspend inline fun <reified T> put(url: String): T {
        return client.put(httpUrl + url)
    }

    suspend inline fun <reified T, K : Any> put(url: String, body: K): T {
        return client.put(httpUrl + url) {
            this.body = body
        }
    }

    suspend inline fun <reified T> delete(url: String): T {
        return client.delete(httpUrl + url)
    }

    fun <T> emit(event: OutboundEvent<T>, data: T) {
        val packet = Packet(event.eventName, data)
        val jsonString = GSON.toJson(packet)
        logger.finer("Emitting outbound packet. Type ${event.eventName}, Data: $jsonString")

        socket.send(jsonString.zlibCompress().asList())
    }

    fun loadHttp() {
        val httpConfig = config.getConfigurationSection("http") ?: throw MissingConfigPathException("api.http")

        httpUrl = httpConfig.getString("url") ?: throw MissingConfigPathException("api.http.url")
        //TODO: auth -> set default header.
    }

    fun loadSocket() {
        val socketConfig = config.getConfigurationSection("socket") ?: throw MissingConfigPathException("api.socket")

        val socketUrl = socketConfig.getString("url") ?: throw MissingConfigPathException("api.socket.url")
        val socketSecret = socketConfig.getString("secret") ?: throw MissingConfigPathException("api.socket.secret")

        createSocket(socketUrl, serverId, socketSecret)
    }

    private fun createSocket(url: String, serverId: String, secret: String) {
        logger.info("Connecting to socket...")

        val okHttp = OkHttpClient()

        val scarlet = Scarlet.Builder()
            .webSocketFactory(okHttp.newWebSocketFactory("$url/minecraft?id=$serverId&token=$secret"))
            .addMessageAdapterFactory(GsonMessageAdapter.Factory())
            .addStreamAdapterFactory(CoroutinesStreamAdapterFactory())
            .build()

        socket = scarlet.create()

        Bukkit.getPluginManager().callEvent(ApiConnectedEvent(this))

        logger.info("Connected.")
    }

}