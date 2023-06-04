package network.warzone.mars.player.achievements.variants.objectives

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.achievements.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object WoolDestroyAchievement {
    //TODO: Does the destroyableBlockDestroys statistic distinguish between wool and other destroyables?
    // If not, then this may not be possible to implement without comparing with the blocksBroken statistic.
    /**fun createAchievement(targetBreaks: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Insert your achievement description here"
            override val gamemode: String = "Insert the gamemode here"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(killer.toString());
                val playerName = context.player.name.toString();

                if (profile != null) {
                    if (profile.stats.objectives.destroyableDestroys >= targetBreaks) {
                        AchievementEmitter.emit(profile, killer.player.get().simple, achievement)
                    }
                }
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }**/
}