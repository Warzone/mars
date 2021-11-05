package network.warzone.mars.api.socket.models

import network.warzone.mars.match.models.DeathCause
import java.util.*

data class SimplePlayer(val id: UUID, val name: String)

data class PartyJoinData(val playerId: UUID, val playerName: String, val partyName: String)

data class PartyLeaveData(val playerId: UUID, val playerName: String)

data class PlayerDeathData(
    val victimId: UUID,
    val victimName: String,
    val attackerId: UUID?,
    val attackerName: String?,
    val weapon: String?,
    val entity: String?,
    val distance: Long?,
    val key: String,
    val cause: DeathCause
)

data class KillStreakData(val amount: Int, val player: SimplePlayer, val ended: Boolean)