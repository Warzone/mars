@file:UseSerializers(UUIDSerializer::class)
package network.warzone.pgm.player.feature

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import network.warzone.pgm.api.ErrorCode
import network.warzone.pgm.api.http.Response
import network.warzone.pgm.feature.Service
import network.warzone.pgm.player.models.PlayerProfile
import network.warzone.pgm.player.models.Session
import network.warzone.pgm.utils.UUIDSerializer
import org.bukkit.entity.Player
import java.util.*

object PlayerService : Service<PlayerProfile>() {

    suspend fun login(player: Player, ip: String): PlayerLoginResponse {
        return apiClient.post("/mc/players/login", PlayerLoginRequest(
            playerId = player.uniqueId,
            playerName = player.name,
            ip = ip
        ))
    }

    suspend fun logout(uuid: UUID, playtime: Long) {
        apiClient.post<Unit, PlayerLogoutRequest>("/mc/players/logout", PlayerLogoutRequest(
            playerId = uuid,
            playtime = playtime
        ))
    }

    override suspend fun get(target: String): PlayerProfile {
        return apiClient.get("/mc/player/$target")
    }

    @Serializable
    data class PlayerLoginRequest(val playerId: UUID, val playerName: String, val ip: String)
    @Serializable
    data class PlayerLoginResponse(
        val player: PlayerProfile,
        val activeSession: Session,

        override val code: ErrorCode? = null,
        override val message: String? = null,
        override val error: Boolean = false
        ) : Response()

    @Serializable
    data class PlayerLogoutRequest(val playerId: UUID, val playtime: Long)

}