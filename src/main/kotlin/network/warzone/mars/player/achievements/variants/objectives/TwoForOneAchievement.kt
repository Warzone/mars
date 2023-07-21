package network.warzone.mars.player.achievements.variants.objectives

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.hasMode
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.goals.events.GoalCompleteEvent
import java.util.*


//TODO: This achievement is not finished.
object TwoForOneAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Capture two wool objectives in a single CTW match."
            override val gamemode: String = "CTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            val objectivesCapturedBy: MutableMap<UUID, Int> = mutableMapOf()

            @EventHandler
            fun onGoalComplete(event: GoalCompleteEvent) = runBlocking {
                AchievementManager.sendDebugStartMessage(achievement, "onGoalComplete")
                if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL)) return@runBlocking
                if (event.contributions.size != 1) return@runBlocking // More than one player contributed.

                val matchPlayer = event.contributions[0].playerState.player.get()
                val context = PlayerManager.getPlayer(matchPlayer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking

                // Increment the count of objectives captured by this player
                val objectivesCaptured = objectivesCapturedBy.getOrDefault(matchPlayer.id, 0) + 1
                objectivesCapturedBy[matchPlayer.id] = objectivesCaptured

                // If this player has captured two objectives, award the achievement
                if (objectivesCaptured == 2) {
                    AchievementEmitter.emit(profile, matchPlayer.simple, achievement)
                }
                AchievementManager.sendDebugFinishMessage(achievement, "onGoalComplete")
            }


            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}