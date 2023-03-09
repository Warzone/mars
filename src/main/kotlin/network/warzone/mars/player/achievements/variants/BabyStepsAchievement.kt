package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.achievement.Achievement
import network.warzone.mars.achievement.AchievementAgent
import network.warzone.mars.achievement.AchievementEmitter
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object BabyStepsAchievement {
    fun createBabyStepsAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "You've killed your first player!"
            override val gamemode: String = "NONE"

            override fun load() {
                Mars.registerEvents(this)
            }
            
            @EventHandler(priority = EventPriority.HIGHEST)
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(context.player.name.toString())

                println("context.player.name.toString() = " + context.player.name.toString());


                if (profile != null) {
                    println("profile.stats.kills = " + profile.stats.kills);
                    if (profile.stats.kills != 0) {
                        AchievementEmitter.emit(killer.player.get(), achievement)
                    }
                }
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
        }
}