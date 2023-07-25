package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

class KillstreakAchievement (val params: AgentParams.KillStreakAgentParams, val achievement: Achievement) : AchievementAgent, Listener {
    override fun load() {
        Mars.registerEvents(this)
    }

    @EventHandler
    fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
        val killer = event.killer ?: return@runBlocking
        val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
        val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking

        if (profile.stats.killstreaks.containsKey(params.targetStreak)) {
            AchievementEmitter.emit(profile, killer.simple, achievement)
        }
    }

    override fun unload() {
        HandlerList.unregisterAll(this)
    }
}