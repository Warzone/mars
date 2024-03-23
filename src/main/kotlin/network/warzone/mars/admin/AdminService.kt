package network.warzone.mars.admin

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.utils.parseHttpException

object AdminService {
    private val serverId = Mars.get().serverId
    private var events: ServerEvents? = null

    init {
        runBlocking {
            events = fetchCurrentEvents()
        }
    }

    fun getCurrentEvents(): ServerEvents? {
        return events
    }

    suspend fun fetchCurrentEvents(): ServerEvents? {
        val request = parseHttpException { ApiClient.get<ServerEvents>("/mc/servers/$serverId/events") }
        val eventsResponse = request.get()!!
        events = eventsResponse
        return events
    }

    suspend fun setXPMultiplier(value: Float, player: SimplePlayer?): ServerEvents? {
        val request = parseHttpException { ApiClient.put<ServerEvents, XPMultiplierRequest>("/mc/servers/$serverId/events/xp_multiplier", XPMultiplierRequest(value, player)) }
        val eventsResponse = request.get()!!
        events = eventsResponse
        return events
    }
}