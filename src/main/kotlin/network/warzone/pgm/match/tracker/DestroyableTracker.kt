package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.api.socket.models.DestroyableDamageData
import network.warzone.pgm.api.socket.models.DestroyableDestroyData
import network.warzone.pgm.match.models.Contribution
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.destroyable.DestroyableHealthChangeEvent

class DestroyableTracker : Listener {

    @EventHandler
    fun onDestroyableDamaged(event: DestroyableHealthChangeEvent) {
        val change = event.change ?: return
        val player = change.playerCause ?: return

        ApiClient.emit(OutboundEvent.DestroyableDamage, DestroyableDamageData(event.destroyable.id, player.id))

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