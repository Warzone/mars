package network.warzone.mars.sit.listeners

import network.warzone.mars.Mars
import network.warzone.mars.sit.SitController
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.spigotmc.event.entity.EntityDismountEvent

class DismountListener : Listener {

    val sitController = Mars.sitController

    @EventHandler
    fun onDismount(event: EntityDismountEvent) {
        if (event.entity !is Player && event.dismounted !is ArmorStand) {
            return
        } else {
            println("Player dismounted so removed from sit list")
            println(sitController.sitting)
            val player = event.entity as Player;
            val seat = event.dismounted as ArmorStand
            seat.remove()
            sitController.unSit(player, seat);
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (sitController.isSitting(event.player)) {
            sitController.unSit(event.player)
            println("Player quit removed from sit list")
        }
    }
}