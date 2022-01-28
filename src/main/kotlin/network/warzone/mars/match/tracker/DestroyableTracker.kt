package network.warzone.mars.match.tracker

import net.minecraft.server.v1_8_R3.Tuple
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.DestroyableDamageData
import network.warzone.mars.api.socket.models.DestroyableDestroyData
import network.warzone.mars.match.models.Contribution
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.destroyable.Destroyable
import tc.oc.pgm.destroyable.DestroyableHealthChangeEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DestroyableTracker : Listener {

    internal class DestroyableDamageBatch(
        val destroyable: Destroyable,
        var count: Int
    )

    private val BATCHING_WINDOW = 10L

    private val damageCache: ConcurrentHashMap<UUID, MutableList<DestroyableDamageBatch>> = ConcurrentHashMap()

    @EventHandler
    fun onDestroyableDamaged(event: DestroyableHealthChangeEvent) {
        val change = event.change ?: return
        val player = change.playerCause ?: return

        if (damageCache[player.id] == null) damageCache[player.id] = mutableListOf()
        val batchList = damageCache[player.id] ?: return

        val destroyableBatch = batchList.find { it.destroyable == event.destroyable }
        if (destroyableBatch == null) {
            batchList.add(DestroyableDamageBatch(event.destroyable, 1))
            Bukkit.getScheduler().runTaskLaterAsynchronously(Mars.get(), {
                val batch = batchList.find { it.destroyable == event.destroyable } ?: return@runTaskLaterAsynchronously
                ApiClient.emit(
                    OutboundEvent.DestroyableDamage,
                    DestroyableDamageData(
                        event.destroyable.id,
                        player.id,
                        batch.count
                    )
                )
                batchList.remove(batch)
            }, BATCHING_WINDOW)
        } else {
            destroyableBatch.count++
        }

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