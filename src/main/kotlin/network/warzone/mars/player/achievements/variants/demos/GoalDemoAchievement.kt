package network.warzone.mars.player.achievements.variants.demos

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementManager
import network.warzone.mars.player.achievements.AchievementParent
import network.warzone.mars.utils.hasMode
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.goals.events.GoalCompleteEvent
import tc.oc.pgm.goals.events.GoalStatusChangeEvent

object GoalDemoAchievement {
    fun createGoalDemoAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "A demo to show things pertaining to a goal."
            // TODO: Change gamemode from String to GamemodeEnum for all variants
            override val gamemode: String = "Insert the gamemode here"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onGoalComplete(event: GoalCompleteEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onGoalComplete called")

                AchievementManager.sendDebugMessage(achievement.name + " Contributions:")
                event.contributions.forEach {
                    AchievementManager.sendDebugMessage("- Name: " + it.playerState.nameLegacy)
                    AchievementManager.sendDebugMessage("- Party: " + it.playerState.party.nameLegacy)
                }

                AchievementManager.sendDebugMessage(achievement.name + ".onGoalComplete finished")
            }

            @EventHandler
            fun onGoalStatusChange(event: GoalStatusChangeEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onGoalStatusChanged called")

                AchievementManager.sendDebugMessage(achievement.name + " Gamemodes: ")
                event.match.map.gamemodes.forEach {
                    AchievementManager.sendDebugMessage("- " + it.name)
                }
                AchievementManager.sendDebugMessage(achievement.name + " Match Competitors: ")
                event.match.competitors.forEach {
                    AchievementManager.sendDebugMessage("- " + it.nameLegacy)
                }

                AchievementManager.sendDebugMessage(achievement.name + ".event.competitor?.nameLegacy = " + event.competitor?.nameLegacy)
                AchievementManager.sendDebugMessage(achievement.name + ".event.goal.isRequired = " + event.goal.isRequired)
                AchievementManager.sendDebugMessage(achievement.name + ".event.goal.isCompleted = " + event.goal.isCompleted)
                AchievementManager.sendDebugMessage(achievement.name + ".event.goal.isShared = " + event.goal.isShared)

                AchievementManager.sendDebugMessage(achievement.name + ".onGoalStatusChanged finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}

//TODO: In various achievement variants, I use fetch() to fetch something from the API.
// I remember having a conversation with tank where he mentioned it wasn't good to fetch
// from the API I think. Will need to clarify this and provide a fix if needed.