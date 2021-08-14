package network.warzone.pgm.api.socket

import network.warzone.pgm.match.models.PartyData
import java.util.*

data class MatchLoadData(val mapId: UUID, val parties: List<PartyData>)
data class PartyJoinData(val playerId: UUID, val playerName: String, val partyName: String)
data class PartyLeaveData(val playerId: UUID, val playerName: String)
data class MatchEndData(val winningTeam: String)
data class MatchStartData(val participants: Set<LiveMatchPlayer>)

sealed class OutboundEvent<T>(val eventName: String) {
    object MatchLoad : OutboundEvent<MatchLoadData>("MATCH_LOAD")
    object MatchStart : OutboundEvent<MatchStartData>("MATCH_START")
    object MatchEnd : OutboundEvent<Unit>("MATCH_END")
    object PartyJoin : OutboundEvent<PartyJoinData>("PARTY_JOIN")
    object PartyLeave : OutboundEvent<PartyLeaveData>("PARTY_LEAVE")
}