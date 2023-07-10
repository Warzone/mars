package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object BabyStepsAchievement {
    fun createBabyStepsAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Kill a player for the first time in any match."
            override val gamemode: String = "NONE"
            override val id: String = achievement.name

            override fun load() {
                Mars.registerEvents(this)
            }
            
            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath called")
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking;

                if (profile.stats.kills != 0) {
                    AchievementEmitter.emit(profile, killer.simple, achievement)
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
        }
}