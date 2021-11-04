package network.warzone.mars.api.socket.models

import java.util.*

data class ControlPointPartial( val id: String, val name: String )
data class PointCaptureData( val pointId: String, val partyName: String, val playerIds: List<UUID> )