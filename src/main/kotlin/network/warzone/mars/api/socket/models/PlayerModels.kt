package network.warzone.mars.api.socket.models

import network.warzone.mars.match.models.DeathCause
import java.util.UUID
import kotlinx.serialization.SerialName
import network.warzone.mars.match.tracker.PlayerLevelUpData
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.KEvent

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

data class PlayerAchievementData(val player: SimplePlayer, val achievement: UUID)

enum class PlayerUpdateReason {
    KILL,
    DEATH,
    CHAT,
    KILLSTREAK,
    KILLSTREAK_END,
    PARTY_JOIN,
    PARTY_LEAVE,
    MATCH_END,
    DESTROYABLE_DAMAGE,
    DESTROYABLE_DESTROY,
    CORE_LEAK,
    FLAG_PLACE,
    FLAG_DROP,
    FLAG_PICKUP,
    FLAG_DEFEND,
    WOOL_PLACE,
    WOOL_DROP,
    WOOL_PICKUP,
    WOOL_DEFEND,
    CONTROL_POINT_CAPTURE,
}

sealed class PlayerUpdateData {
    @SerialName("KillUpdateData")
    data class KillUpdateData(val data: PlayerDeathData, val firstBlood: Boolean) : PlayerUpdateData()
    @SerialName("ChatUpdateData")
    data class ChatUpdateData(val data: PlayerChatData) : PlayerUpdateData()
    @SerialName("KillstreakUpdateData")
    data class KillstreakUpdateData(val amount: Int) : PlayerUpdateData()
    @SerialName("PartyUpdateData")
    data class PartyUpdateData(val party: String) : PlayerUpdateData()
    @SerialName("MatchEndUpdateData")
    data class MatchEndUpdateData(val data: MatchEndData) : PlayerUpdateData()
    @SerialName("DestroyableDamageUpdateData")
    data class DestroyableDamageUpdateData(val data: DestroyableDamageData, val blockCount: Int) : PlayerUpdateData()
    @SerialName("DestroyableDestroyUpdateData")
    data class DestroyableDestroyUpdateData(val percentage: Float, val blockCount: Int) : PlayerUpdateData()
    @SerialName("CoreLeakUpdateData")
    data class CoreLeakUpdateData(val percentage: Float, val blockCount: Int) : PlayerUpdateData()
    @SerialName("MonumentPlaceUpdateData")
    data class MonumentPlaceUpdateData(val heldTime: Long) : PlayerUpdateData()
    @SerialName("MonumentDropUpdateData")
    data class MonumentDropUpdateData(val heldTime: Long) : PlayerUpdateData()
    @SerialName("ControlPointCaptureUpdateData")
    data class ControlPointCaptureUpdateData(val contributors: Int) : PlayerUpdateData()
    @SerialName("NoArgs")
    object NoArgs : PlayerUpdateData()
}

data class PlayerUpdate(
    val updated: PlayerProfile,
    val data: PlayerUpdateData,
    val reason: PlayerUpdateReason
)

data class PlayerUpdateEvent(val update: PlayerUpdate) : KEvent()