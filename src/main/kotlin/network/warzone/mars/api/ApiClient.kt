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
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.Mars
import network.warzone.mars.api.events.ApiConnectedEvent
import network.warzone.mars.api.socket.*
import network.warzone.mars.api.socket.models.MessageData
import network.warzone.mars.api.socket.models.MessageEvent
import network.warzone.mars.api.socket.models.PlayerChatData
import network.warzone.mars.api.socket.models.PlayerChatEvent
import network.warzone.mars.match.tracker.ForceMatchEndEvent
import network.warzone.mars.match.tracker.PlayerXPGainData
import network.warzone.mars.match.tracker.PlayerXPGainEvent
import network.warzone.mars.player.feature.DisconnectPlayerData
import network.warzone.mars.player.feature.DisconnectPlayerEvent
import network.warzone.mars.utils.*
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
                registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
                    Date(json.asJsonPrimitive.asLong)
                })
                registerTypeAdapter(Date::class.java, JsonSerializer<Date> { date, _, _ ->
                    JsonPrimitive(date.time)
                })
                registerTypeAdapter(
                    AgentParams::class.java,
                    ClosedPolymorphicArrayDeserializer.createFromSealedClass(AgentParams::class)
                )
            }
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            header("Authorization", "API-Token $apiToken")
            header("Mars-Server-ID", Mars.get().serverId)
        }
    }

    lateinit var socket: WarzoneService
    lateinit var baseUrl: String
    private lateinit var apiToken: String

    init {
        logger.level = Level.ALL
    }

    suspend fun loadHttp(config: ConfigurationSection) {
        val httpConfig = config.getConfigurationSection("http") ?: throw MissingConfigPathException("api.http")

        baseUrl = httpConfig.getString("url") ?: throw MissingConfigPathException("api.http.url")
        apiToken = config.getString("secret") ?: throw MissingConfigPathException("api.secret")

        // Send startup request
        post<Unit>("/mc/servers/${Mars.get().serverId}/startup")
    }

    fun loadSocket(serverId: String, config: ConfigurationSection) {
        val socketConfig = config.getConfigurationSection("socket") ?: throw MissingConfigPathException("api.socket")

        val socketUrl = socketConfig.getString("url") ?: throw MissingConfigPathException("api.socket.url")

        createSocket(socketUrl, serverId, apiToken)
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
        Mars.async {
            val packet = Packet(outboundEvent.event, data)

            socket.send(packet)
        }
    }

    fun <T : Any> emitBlocking(outboundEvent: OutboundEvent<T>, data: T) {
        val packet = Packet(outboundEvent.event, data)

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
//                println("Received event ${it.event}, data: ${it.data}")
                val json = GSON.toJson(it.data)
                when (it.event) {
                    SocketEventType.PLAYER_CHAT -> {
                        val data = GSON.fromJson(json, PlayerChatData::class.java)
                        PlayerChatEvent(data).callEvent()
                    }
                    SocketEventType.MESSAGE -> {
                        val data = GSON.fromJson(json, MessageData::class.java)
                        MessageEvent(data).callEvent()
                    }
                    SocketEventType.PLAYER_XP_GAIN -> {
                        val data = GSON.fromJson(json, PlayerXPGainData::class.java)
                        PlayerXPGainEvent(data).callEvent()
                    }
                    SocketEventType.FORCE_MATCH_END -> ForceMatchEndEvent().callEvent()
                    SocketEventType.DISCONNECT_PLAYER -> {
                        val data = GSON.fromJson(json, DisconnectPlayerData::class.java)
                        DisconnectPlayerEvent(data).callEvent()
                    }
                }
            }

        Bukkit.getPluginManager().callEvent(ApiConnectedEvent(this))

        logger.info("Connected.")
    }

}