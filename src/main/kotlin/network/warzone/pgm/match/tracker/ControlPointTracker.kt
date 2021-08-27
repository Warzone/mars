package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.api.socket.models.PointCaptureData
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