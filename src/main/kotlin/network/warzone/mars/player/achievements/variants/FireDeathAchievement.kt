package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

class FireDeathAchievement(val achievement: Achievement) : AchievementAgent, Listener {
    @EventHandler
    fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
        val player = event.victim ?: return@runBlocking
        val context = PlayerManager.getPlayer(player.id) ?: return@runBlocking
        val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking
        val cause = context.player.lastDamageCause.cause

        if (   cause == EntityDamageEvent.DamageCause.FIRE
            || cause == EntityDamageEvent.DamageCause.FIRE_TICK
            || cause == EntityDamageEvent.DamageCause.LAVA) {
            AchievementEmitter.emit(profile, player.simple, achievement)
        }
    }
}