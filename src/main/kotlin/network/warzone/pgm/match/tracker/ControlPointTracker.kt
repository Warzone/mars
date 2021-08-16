package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.controlpoint.events.ControllerChangeEvent
import java.util.*

class ControlPointTracker : Listener {

    data class ControlPointPartial( val id: String, val name: String )
    data class PointCaptureData( val pointId: String, val partyName: String, val playerIds: List<UUID> )

    object PointCapture : OutboundEvent<PointCaptureData>("CONTROL_POINT_CAPTURE")

    @EventHandler
    fun onHillCapture(event: ControllerChangeEvent) {
        val team = event.newController ?: return

        val thieves = event.controlPoint.playerTracker
            .playersOnPoint
            .filter { team.players.contains(it) }
            .map { it.id }

        ApiClient.emit(
            PointCapture,
            PointCaptureData(event.controlPoint.id, event.newController!!.nameLegacy, thieves)
        )
    }

}