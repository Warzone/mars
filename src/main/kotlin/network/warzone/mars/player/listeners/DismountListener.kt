package network.warzone.mars.player.listeners

import network.warzone.mars.Mars
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.spigotmc.event.entity.EntityDismountEvent

class DismountListener : Listener {

    val sitController = Mars.Companion.sitController

    @EventHandler
    fun onDismount(event: EntityDismountEvent) {
        if (event.entity !is Player && event.dismounted !is ArmorStand) {
            return
        } else {
            val player = event.entity as Player;
            sitController.unSit(player);
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (sitController.isSitting(event.player)) {
            sitController.unSit(event.player)
        }
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        sitController.unSit(player);
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        for ((uuid) in sitController.sitting) {
            val player = Bukkit.getPlayer(uuid)
            if (block == sitController.sitting.get(player.uniqueId)?.second) {
                sitController.unSit(player)
            }
        }
    }

}