package network.warzone.pgm.api.socket.models

import network.warzone.pgm.match.models.LiveMatchPlayer
import network.warzone.pgm.match.models.PartyData
import network.warzone.pgm.match.tracker.PlayerBlocks
import network.warzone.pgm.match.tracker.PlayerMessages
import java.util.*
import kotlin.collections.HashMap

data class MatchLoadGoals(
    val flags: List<FlagPartial>,
    val wools: List<WoolPartial>,
    val cores: List<CorePartial>,
    val controlPoints: List<ControlPointPartial>,
    val destroyables: List<DestroyablePartial>
)

data class MatchLoadData(
    val mapId: UUID,
    val parties: List<PartyData>,
    val goals: MatchLoadGoals
)

data class MatchStartData(val participants: Set<LiveMatchPlayer>)
data class MatchEndData(val winningParties: List<String>, val bigStats: Map<UUID, BigStats>)

data class BigStats(
    var blocks: PlayerBlocks?,
    var messages: PlayerMessages,
    var bowShotsTaken: Int = 0,
    var bowShotsHit: Int = 0,
    var damageGiven: Double = 0.0,
    var damageTaken: Double = 0.0,
    var damageGivenBow: Double = 0.0
) {
    fun isDefault(): Boolean {
        return blocks === null && messages.global == 0 && messages.staff == 0 && messages.team == 0 && bowShotsTaken == 0 && bowShotsHit == 0 && damageGiven == 0.0 && damageTaken == 0.0 && damageGivenBow == 0.0
    }
}