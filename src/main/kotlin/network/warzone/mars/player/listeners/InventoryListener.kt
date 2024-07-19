package network.warzone.mars.player.listeners

import network.warzone.mars.utils.getMatch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCreativeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import tc.oc.pgm.api.PGM
import tc.oc.pgm.spawns.events.ObserverKitApplyEvent

fun isInObserverMode(player: Player): Boolean {
    val match = PGM.get().matchManager.getMatch()
    val isObserving = match.observers.any { it.bukkit.uniqueId == player.uniqueId }
    return isObserving || match.isFinished
}

class InventoryListener : Listener {
    /**
     * Prevent the player from modifying their inventory while spectating
     */
    @EventHandler
    fun onObserverInventoryChange(event: InventoryCreativeEvent) {
        val player = event.view.player as Player
        if (isInObserverMode(player)) event.isCancelled = true
    }

    /**
     * Prevent the player from dropping items while spectating
     */
    @EventHandler
    fun onObserverItemDrop(event: PlayerDropItemEvent) {
        val player = event.player
        if (isInObserverMode(player)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onObserverKitApply(event: ObserverKitApplyEvent) {
        val player = event.player

        val inventory = player.inventory ?: return

        // Remove extra staff items from observer inventory
        inventory.setItem(1, null)
        inventory.setItem(4, null)
        inventory.setItem(6, null)
    }
}
