package network.warzone.pgm.api.socket.models

import network.warzone.pgm.match.models.LiveMatchPlayer
import network.warzone.pgm.match.models.PartyData
import java.util.*

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
data class MatchEndData(val winningParty: String?)
