package network.warzone.mars.sit.listeners

import network.warzone.mars.Mars
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
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
            val player = event.entity as Player;
            val seat = event.dismounted as ArmorStand
//            seat.remove()
            sitController.unSit(player, seat);
            println(sitController.sitting)

        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (sitController.isSitting(event.player)) {
            sitController.unSit(event.player)
            println("Player quit removed from sit list")
        }
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        sitController.unSit(player);
    }
}