package network.warzone.mars.broadcast

import network.warzone.mars.api.ApiClient

object BroadcastService {
    suspend fun getBroadcasts(): List<Broadcast> {
        return ApiClient.get("/mc/broadcasts")
    }
}