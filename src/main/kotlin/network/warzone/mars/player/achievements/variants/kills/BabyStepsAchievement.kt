package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.achievements.AchievementParent
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
            override val description: String = "You've killed your first player!"
            override val gamemode: String = "NONE"
            override val id: String = achievement.name

            override fun load() {
                Mars.registerEvents(this)
            }
            
            @EventHandler(priority = EventPriority.HIGHEST)
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val playerName = context.player.name.toString();
                val profile = PlayerFeature.fetch(playerName)

                println("context.player.name.toString() = " + context.player.name.toString());

                context.matchPlayer.simple

                if (profile != null) {
                    println("profile.stats.kills = " + profile.stats.kills);
                    if (profile.stats.kills != 0) {
                        AchievementEmitter.emit(profile, context.matchPlayer.simple, achievement)
                    }
                }
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
        }
}