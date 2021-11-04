package network.warzone.mars.api

import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import network.warzone.mars.api.events.ApiConnectedEvent
import network.warzone.mars.api.socket.InboundEvent
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.SocketEventType
import network.warzone.mars.api.socket.WarzoneService
import network.warzone.mars.utils.GSON
import network.warzone.mars.utils.GsonMessageAdapter
import network.warzone.mars.utils.MissingConfigPathException
import network.warzone.mars.utils.createLogger
import okhttp3.OkHttpClient
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import java.util.logging.Level

data class Packet<T>(
    @SerializedName("e") val event: SocketEventType,
    @SerializedName("d") val data: T,
)

object ApiClient {

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
    lateinit var baseUrl: String

    init {
        logger.level = Level.ALL
    }

    fun loadHttp(config: ConfigurationSection) {
        val httpConfig = config.getConfigurationSection("http") ?: throw MissingConfigPathException("api.http")

        baseUrl = httpConfig.getString("url") ?: throw MissingConfigPathException("api.http.url")
        //TODO: auth -> set default header.
    }

    fun loadSocket(serverId: String, config: ConfigurationSection) {
        val socketConfig = config.getConfigurationSection("socket") ?: throw MissingConfigPathException("api.socket")

        val socketUrl = socketConfig.getString("url") ?: throw MissingConfigPathException("api.socket.url")
        val socketSecret = socketConfig.getString("secret") ?: throw MissingConfigPathException("api.socket.secret")

        createSocket(socketUrl, serverId, socketSecret)
    }

    suspend inline fun <reified T> get(url: String): T {
        return client.get(baseUrl + url)
    }

    suspend inline fun <reified T> post(url: String): T {
        return client.post(baseUrl + url)
    }

    suspend inline fun <reified T, K : Any> post(url: String, body: K): T {
        return client.post(baseUrl + url) {
            this.body = body
        }
    }

    suspend inline fun <reified T> put(url: String): T {
        return client.put(baseUrl + url)
    }

    suspend inline fun <reified T, K : Any> put(url: String, body: K): T {
        return client.put(baseUrl + url) {
            this.body = body
        }
    }

    suspend inline fun <reified T> delete(url: String): T {
        return client.delete(baseUrl + url)
    }

    fun <T : Any> emit(outboundEvent: OutboundEvent<T>, data: T) {
        val packet = Packet(outboundEvent.event, data)
        logger.finer("Emitting outbound packet. Type ${outboundEvent.event}, Data: ${GSON.toJson(packet)}")

        socket.send(packet)
    }

    private fun createSocket(url: String, serverId: String, secret: String) {
        logger.info("Connecting to socket...")

        val okHttp = OkHttpClient()

        val scarlet = Scarlet.Builder()
            .webSocketFactory(okHttp.newWebSocketFactory("$url/minecraft?id=$serverId&token=$secret"))
            .addMessageAdapterFactory(GsonMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .build()

        socket = scarlet.create()

        socket.receive()
            .subscribe {
                val evt: InboundEvent<Any> = InboundEvent.get(it.event) ?: return@subscribe
                evt.bukkitEventFactory.invoke(it.data).callEvent()
            }

        Bukkit.getPluginManager().callEvent(ApiConnectedEvent(this))

        logger.info("Connected.")
    }

}