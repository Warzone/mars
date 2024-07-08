package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.Agent
import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.models.AchievementCategory
import network.warzone.mars.player.achievements.variants.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.event.MatchFinishEvent

// A class for managing various achievement-related functionalities.
object AchievementManager : Listener {
    private val achievementToAgent: MutableMap<Achievement, AchievementAgent> = mutableMapOf()

    fun load() {
        Mars.registerEvents(this)
        fetchNewAchievements()
    }

    // Input a list of achievements, return a list of distinct achievement categories amongst those achievements.
    fun getCategoriesFromAchievements(achievements: List<Achievement>) : List<AchievementCategory> {
        return achievements.mapNotNull { it.category }.distinct()
    }

    // Input a category, return a list of achievements of that category.
    fun getAchievementsForCategory(category: String): List<Achievement> {
        return achievementToAgent.keys.filter { it.category?.category == category }
    }

    // Input an achievement category and a list of achievements, return the achievements containing that category.
    fun filterAchievementsWithCategory(category: AchievementCategory, achievements: List<Achievement>) : List<Achievement>{
        return achievements.filter { it.category == category }

    }

    // Update achievements every time a match ends.
    @EventHandler
    fun onMatchFinish(event: MatchFinishEvent) {
        fetchNewAchievements()
    }

    fun unload() {
        achievementToAgent.values.forEach { it.unload() }
        achievementToAgent.clear()
    }

    // Create an achievement agent based on an achievement's agent params, and return a reference
    // to the newly created agent.
    private fun createAgentForAchievement(achievement: Achievement) : AchievementAgent {
        val emitter = AchievementEmitter(achievement)
        val agent = when (val agentParams = achievement.agent) {
            is Agent.KillStreakAgentParams -> {
                KillstreakAchievement(agentParams.targetStreak, emitter)
            }
            is Agent.TotalKillsAgentParams -> {
                TotalKillsAchievement(agentParams.targetKills, emitter)
            }
            is Agent.FireDeathAgentParams -> {
                FireDeathAchievement(emitter)
            }
            is Agent.CaptureNoSprintAgentParams -> {
                CaptureNoSprintAchievement(emitter)
            }
            is Agent.ChatMessageAgentParams -> {
                ChatMessageAchievement(agentParams.message, emitter)
            }
            is Agent.LevelUpAgentParams -> {
                LevelUpAchievement(agentParams.level, emitter)
            }
            is Agent.WoolCaptureAgentParams -> {
                WoolCaptureAchievement(agentParams.captures, emitter)
            }
            is Agent.FirstBloodAgentParams -> {
                FirstBloodAchievement(agentParams.target, emitter)
            }
            is Agent.BowDistanceAgentParams -> {
                BowDistanceAchievement(agentParams.distance, emitter)
            }
            is Agent.FlagCaptureAgentParams -> {
                FlagCaptureAchievement(agentParams.captures, emitter)
            }
            is Agent.FlagDefendAgentParams -> {
                FlagDefendAchievement(agentParams.defends, emitter)
            }
            is Agent.WoolDefendAgentParams -> {
                WoolDefendAchievement(agentParams.defends, emitter)
            }
            is Agent.MonumentDamageAgentParams -> {
                MonumentDamageAchievement(agentParams.breaks, emitter)
            }
            is Agent.KillConsecutiveAgentParams -> {
                KillConsecutiveAchievement(agentParams, emitter)
            }
            is Agent.PlayTimeAgentParams -> {
                PlayTimeAchievement(agentParams.hours, emitter)
            }
            is Agent.RecordAgentParams<*> -> {
                RecordAchievement(agentParams as Agent.RecordAgentParams<Number>, emitter)
            }
            is Agent.ControlPointCaptureAgentParams -> {
                ControlPointCaptureAchievement(agentParams.captures, emitter)
            }
            is Agent.TotalWinsAgentParams -> {
                TotalWinsAchievement(agentParams.wins, emitter)
            }
            is Agent.TotalDeathsAgentParams -> {
                TotalDeathsAchievement(agentParams.deaths, emitter)
            }
            is Agent.TotalLossesAgentParams -> {
                TotalLossesAchievement(agentParams.losses, emitter)
            }
            // ...
            else -> throw IllegalArgumentException("Unknown AgentParams for achievement ${achievement.name}")
        }
        achievementToAgent[achievement] = agent
        return agent
    }

    // Activates/deactivates achievements based on certain conditions.
    // Also used to initialize achievements when the server starts.
    private fun fetchNewAchievements() {
        Mars.async {
            // Fetch the current achievements from the API database
            val currentAchievements = AchievementFeature.list()

            // Find achievements from the database that are currently not loaded on the server.
            val newAchievements = currentAchievements.filter { it !in achievementToAgent.keys }

            // Find achievements that are currently loaded on the server, but no longer in the database.
            val achievementsToRemove = achievementToAgent.keys.filter { it !in currentAchievements }

            // Add new achievements (if any).
            if (newAchievements.isNotEmpty()) {
                newAchievements.forEach { achievement ->
                    val agent = createAgentForAchievement(achievement)
                    agent.load()
                    achievementToAgent[achievement] = agent
                }
            }

            // Disable and remove deleted achievements (if any).
            if (achievementsToRemove.isNotEmpty()) {
                achievementsToRemove.forEach { achievement ->
                    achievementToAgent[achievement]?.unload()
                    achievementToAgent.remove(achievement)
                }
            }
        }
    }
}
