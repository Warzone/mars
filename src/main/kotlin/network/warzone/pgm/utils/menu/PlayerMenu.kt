package network.warzone.pgm.utils.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent

open class PlayerMenu(name: String, rows: Int, val player: Player) : Menu(name, rows) {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (event.player.uniqueId.equals(player.uniqueId)) super.disable()
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.equals(inventory) && event.player.uniqueId.equals(player.uniqueId)) super.disable()
    }

}