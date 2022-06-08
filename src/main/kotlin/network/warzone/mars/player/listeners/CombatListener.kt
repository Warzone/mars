package network.warzone.mars.player.listeners

import network.warzone.mars.utils.matchPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import java.text.DecimalFormat

class CombatListener : Listener {
    private val decimalFormat = DecimalFormat("0.0")

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.PROJECTILE) return
        if (event.entity !is Player) return

        val shooter = (event.damager as? Projectile)?.shooter as? Player ?: return
        val damaged = event.entity as Player

        // If the place shoot themselves, don't show the health
        if (shooter == damaged) return

        val health = damaged.health - event.finalDamage

        // If player is observing or if health is negative, don't send message
        if (damaged.matchPlayer.isObserving || health < 0) return

        val name = damaged.matchPlayer.name
        val component = name.append(text(" • ", NamedTextColor.DARK_GRAY)).append(text("❤ ${decimalFormat.format(health)}", NamedTextColor.RED))
        shooter.matchPlayer.sendActionBar(component)
    }
}