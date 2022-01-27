package network.warzone.mars.player.listeners

import net.kyori.adventure.title.Title
import network.warzone.mars.utils.matchPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import java.text.DecimalFormat

class CombatListener : Listener {
    val DECIMAL_FORMAT = DecimalFormat("0.0")

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.PROJECTILE) return
        if (event.entity !is Player) return

        val shooter = (event.damager as? Projectile)?.shooter as? Player ?: return
        val damaged = event.entity as Player

        val health = damaged.health - event.finalDamage

        // If player is observing or if health is negative, don't send message
        if (damaged.matchPlayer.isObserving || health < 0) return

        val name = damaged.matchPlayer.name
        val component = name.append(text(" • ", NamedTextColor.DARK_GRAY)).append(text("❤ ${DECIMAL_FORMAT.format(health)}", NamedTextColor.RED))
        shooter.matchPlayer.sendActionBar(component)
    }
}