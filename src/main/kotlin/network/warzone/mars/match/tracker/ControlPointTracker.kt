package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PointCaptureData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent

class ControlPointTracker : Listener {
    @EventHandler
    fun onHillCapture(event: ControllerChangeEvent) {
        val team = event.newController ?: return

        val thieves = event.controlPoint.playerTracker
            .playersOnPoint
            .filter { team.players.contains(it) }
            .map { it.id }

        ApiClient.emit(
            OutboundEvent.PointCapture,
            PointCaptureData(event.controlPoint.id, event.newController!!.nameLegacy, thieves)
        )
    }
}