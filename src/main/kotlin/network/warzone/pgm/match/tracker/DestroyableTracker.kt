package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.match.models.Contribution
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent
import tc.oc.pgm.destroyable.DestroyableHealthChangeEvent
import java.util.*

class DestroyableTracker : Listener {

    data class DestroyablePartial( val id: String, val name: String, val ownerName: String, val material: Material, val blockCount: Int )
    data class DestroyableDamageData( val destroyableId: String, val playerId: UUID )
    data class DestroyableDestroyData( val destroyableId: String, val contributions: List<Contribution> )

    object DestroyableDamage : OutboundEvent<DestroyableDamageData>("DESTROYABLE_DAMAGE")
    object DestroyableDestroy : OutboundEvent<DestroyableDestroyData>("DESTROYABLE_DESTROY")

    @EventHandler
    fun onDestroyableDamaged(event: DestroyableHealthChangeEvent) {
        val change = event.change ?: return
        val player = change.playerCause ?: return

        ApiClient.emit(DestroyableDamage, DestroyableDamageData(event.destroyable.id, player.id))
    }

    @EventHandler
    fun onDestroyableDestroyed(event: DestroyableDestroyedEvent) {
        ApiClient.emit(
            DestroyableDestroy,
            DestroyableDestroyData(
                event.destroyable.id,
                event.destroyable
                    .contributions
                    .map { Contribution(it.playerState.id, it.percentage.toFloat(), it.blocks) }
            )
        )
    }

}