package network.warzone.mars.player.achievements.variants.objectives

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.achievements.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.hasMode
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.goals.events.GoalCompleteEvent

object GluttonousAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Destroy an entire wool monument yourself in a DTW match."
            override val gamemode: String = "DTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onGoalComplete(event: GoalCompleteEvent) = runBlocking {
                if (!event.match.hasMode(Gamemode.DESTROY_THE_MONUMENT)) return@runBlocking
                if (event.contributions.size != 1) return@runBlocking // More than one player contributed.

                val matchPlayer = event.contributions[0].playerState.player.get()
                val context = PlayerManager.getPlayer(matchPlayer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking;

                AchievementEmitter.emit(profile, matchPlayer.simple, achievement)
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}