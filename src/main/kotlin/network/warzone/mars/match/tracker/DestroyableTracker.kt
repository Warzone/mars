package network.warzone.mars.match.tracker

import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.DestroyableDamageData
import network.warzone.mars.api.socket.models.DestroyableDestroyData
import network.warzone.mars.match.models.Contribution
import network.warzone.mars.utils.KEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.destroyable.Destroyable
import tc.oc.pgm.destroyable.DestroyableHealthChange
import tc.oc.pgm.destroyable.DestroyableHealthChangeEvent
import tc.oc.pgm.util.material.MaterialData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DestroyableDamageEvent(
    val destroyable: Destroyable,
    val player: MatchPlayer,
    val damage: Int
) : KEvent()

class DestroyableTracker : Listener {

    internal class DestroyableDamageBatch(
        val destroyable: Destroyable,
        val change: DestroyableHealthChange,
        var count: Int
    )

    private val BATCHING_WINDOW = 10L

    private val damageCache: ConcurrentHashMap<UUID, MutableList<DestroyableDamageBatch>> = ConcurrentHashMap()
    private val activeBatches: Queue<BukkitTask> = LinkedList()

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        damageCache.clear()
        activeBatches.clear()
    }

    @EventHandler
    fun onDestroyableDamaged(event: DestroyableHealthChangeEvent) {
        val change = event.change ?: return
        val player = change.playerCause ?: return

        if (damageCache[player.id] == null) damageCache[player.id] = mutableListOf()
        val batchList = damageCache[player.id] ?: return

        val destroyableBatch = batchList.find { it.destroyable == event.destroyable }
        if (destroyableBatch == null) {
            batchList.add(DestroyableDamageBatch(event.destroyable, change, 1))
            val task = Bukkit.getScheduler().runTaskLaterAsynchronously(Mars.get(), Runnable {
                val batch = batchList.find { it.destroyable == event.destroyable } ?: return@Runnable

                resolveBatch(player, batch)

                activeBatches.remove()
            }, BATCHING_WINDOW)

            activeBatches.offer(task)
        } else {
            destroyableBatch.count++
        }

        if (event.destroyable.isDestroyed) {
            val lastBatch = activeBatches.remove()
            Bukkit.getScheduler().cancelTask(lastBatch.taskId)

            val batch = batchList.find { it.destroyable == event.destroyable } ?: return
            resolveBatch(player, batch)

            ApiClient.emit(
                OutboundEvent.DestroyableDestroy,
                DestroyableDestroyData(
                    event.destroyable.id,
                    MaterialData.block(change.oldState.block).itemType.name,
                    event.destroyable
                        .contributions
                        .map { Contribution(it.playerState.id, it.percentage.toFloat(), it.blocks) }
                )
            )
        }
    }

    private fun resolveBatch(player: ParticipantState, batch: DestroyableDamageBatch) {
        ApiClient.emit(
            OutboundEvent.DestroyableDamage,
            DestroyableDamageData(
                batch.destroyable.id,
                MaterialData.block(batch.change.oldState.block).itemType.name,
                player.id,
                batch.count
            )
        )

        damageCache[player.id]?.remove(batch)
        DestroyableDamageEvent(batch.destroyable, player.player.get(), batch.count).callEvent()
    }
}