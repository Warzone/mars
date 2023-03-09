package network.warzone.mars.api.socket.models

import network.warzone.mars.achievement.Achievement
import network.warzone.mars.match.models.DeathCause
import java.util.*

data class SimplePlayer(val id: UUID, val name: String)

data class PartyJoinData(val player: SimplePlayer, val partyName: String)

data class PartyLeaveData(val player: SimplePlayer)

data class PlayerDeathData(
    val victim: SimplePlayer,
    val attacker: SimplePlayer?,
    val weapon: String?,
    val entity: String?,
    val distance: Long?,
    val key: String,
    val cause: DeathCause
)

data class KillstreakData(val amount: Int, val player: SimplePlayer, val ended: Boolean)

data class PlayerAchievementData(val achievement: Achievement, val isComplete: Boolean = false)