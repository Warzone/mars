package network.warzone.mars.admin

import network.warzone.mars.api.socket.models.SimplePlayer
import java.util.*

data class ServerEvents(var xpMultiplier: XPMultiplierResponse?)

data class XPMultiplierResponse(var value: Float, var player: SimplePlayer? = null, var updatedAt: Date)

data class XPMultiplierRequest(var value: Float, var player: SimplePlayer? = null)