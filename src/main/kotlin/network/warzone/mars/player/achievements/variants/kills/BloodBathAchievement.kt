package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object BloodBathAchievement {
    fun createBloodBathAchievement(targetKills: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Obtain ${targetKills} first-blood kills."
            override val gamemode: String = "NONE"
            override val parent: AchievementParent = AchievementParent.BLOOD_BATH
            override val id: String = achievement.name

            override fun load() {
                //Mars.registerEvents(this)
            }

            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(killer.toString());
                val playerName = context.player.name.toString();

                if (profile != null) {
                    if (profile.stats.firstBloods == targetKills) {
                        //AchievementEmitter.emit(profile, playerName, achievement)
                    }
                }
            }

            override fun unload() {
                //HandlerList.unregisterAll(this)
            }
        }
}