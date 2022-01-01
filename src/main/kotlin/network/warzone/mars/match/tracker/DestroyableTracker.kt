package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.DestroyableDamageData
import network.warzone.mars.api.socket.models.DestroyableDestroyData
import network.warzone.mars.match.models.Contribution
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.destroyable.DestroyableHealthChangeEvent

class DestroyableTracker : Listener {

    @EventHandler
    fun onDestroyableDamaged(event: DestroyableHealthChangeEvent) {
        val change = event.change ?: return
        val player = change.playerCause ?: return

        if (event.destroyable.isDestroyed) {
            ApiClient.emit(
                OutboundEvent.DestroyableDestroy,
                DestroyableDestroyData(
                    event.destroyable.id,
                    event.destroyable
                        .contributions
                        .map { Contribution(it.playerState.id, it.percentage.toFloat(), it.blocks) }
                )
            )
        }
    }

}