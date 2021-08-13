package network.warzone.pgm.api.socket

import java.util.*

data class MatchLoadData(val mapId: UUID)

sealed class OutboundEvent<T>(val eventName: String) {
    object MatchLoad : OutboundEvent<MatchLoadData>("MATCH_LOAD")
    object MatchStart : OutboundEvent<Unit>("MATCH_START")
}