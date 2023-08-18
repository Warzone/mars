package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.Mars
import network.warzone.mars.match.MatchManager
import network.warzone.mars.player.achievements.models.AchievementParent
import network.warzone.mars.player.achievements.variants.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.event.MatchFinishEvent

object AchievementManager : Listener, AchievementDebugger {
    private val achievements : MutableList<Achievement> = mutableListOf()
    private val activeAgents : MutableList<AchievementAgent> = mutableListOf()

    fun load() {
        Mars.registerEvents(this)
        fetchNewAchievements()
    }

    fun getParentsFromAchievements(achievements: List<Achievement>) : List<AchievementParent> {
        return achievements.mapNotNull { it.parent }.distinct()
    }

    fun getAchievementsForCategory(category: String): List<Achievement> {
        return achievements.filter { it.parent?.category == category }
    }

    fun filterAchievementsWithParent(parent: AchievementParent, achievements: List<Achievement>) : List<Achievement>{
        return achievements.filter { it.parent == parent }

    }

    @EventHandler
    fun onMatchFinish(event: MatchFinishEvent) {
        fetchNewAchievements()
        activeAgents.forEach { agent -> agent.onMatchFinish(event)  }
    }

    fun unload() {
        activeAgents.forEach { agent -> agent.unload() }
        activeAgents.clear()
        achievements.clear()
    }

    private fun findAgentForAchievement(achievement: Achievement) : AchievementAgent {
        val emitter = AchievementEmitter(achievement)
        return when (val agentParams = achievement.agent.params) {
            is AgentParams.KillStreakAgentParams -> {
                KillstreakAchievement(agentParams.targetStreak, emitter)
            }
            is AgentParams.TotalKillsAgentParams -> {
                TotalKillsAchievement(agentParams.targetKills, emitter)
            }
            is AgentParams.FireDeathAgentParams -> {
                FireDeathAchievement(emitter)
            }
            is AgentParams.CaptureNoSprintAgentParams -> {
                CaptureNoSprintAchievement(emitter)
            }
            is AgentParams.ChatMessageAgentParams -> {
                ChatMessageAchievement(agentParams.message, emitter)
            }
            is AgentParams.LevelUpAgentParams -> {
                LevelUpAchievement(agentParams.level, emitter)
            }
            is AgentParams.WoolCaptureAgentParams -> {
                WoolCaptureAchievement(agentParams.captures, emitter)
            }
            is AgentParams.FirstBloodAgentParams -> {
                FirstBloodAchievement(agentParams.target, emitter)
            }
            is AgentParams.BowDistanceAgentParams -> {
                BowDistanceAchievement(agentParams.distance, emitter)
            }
            is AgentParams.FlagCaptureAgentParams -> {
                FlagCaptureAchievement(agentParams.captures, emitter)
            }
            is AgentParams.FlagDefendAgentParams -> {
                FlagDefendAchievement(agentParams.defends, emitter)
            }
            is AgentParams.WoolDefendAgentParams -> {
                WoolDefendAchievement(agentParams.defends, emitter)
            }
            is AgentParams.MonumentDamageAgentParams -> {
                MonumentDamageAchievement(agentParams.breaks, emitter)
            }
            is AgentParams.KillConsecutiveAgentParams -> {
                KillConsecutiveAchievement(agentParams, emitter)
            }
            is AgentParams.PlayTimeAgentParams -> {
                PlayTimeAchievement(agentParams.hours, emitter)
            }
            is AgentParams.RecordAgentParams<*> -> {
                RecordAchievement(agentParams as AgentParams.RecordAgentParams<Number>, emitter)
            }
            is AgentParams.ControlPointCaptureAgentParams -> {
                ControlPointCaptureAchievement(agentParams.captures, emitter)
            }
            is AgentParams.TotalWinsAgentParams -> {
                TotalWinsAchievement(agentParams.wins, emitter)
            }
            is AgentParams.TotalDeathsAgentParams -> {
                TotalDeathsAchievement(agentParams.deaths, emitter)
            }
            is AgentParams.TotalLossesAgentParams -> {
                TotalLossesAchievement(agentParams.losses, emitter)
            }
            // ...
            else -> throw IllegalArgumentException("Unknown AgentParams for achievement ${achievement.name}")
        }
    }

    //TODO: Deleting an achievement currently requires a restart for it to completely go away.
    private fun fetchNewAchievements() {
        Mars.async {
            // Fetch the current achievements from the API
            val currentAchievements = AchievementFeature.list()

            // Find the achievements that are not in the currently loaded achievements
            val newAchievements = currentAchievements.filter { it !in achievements }

            // If there are new achievements, add them to the list and activate their agents
            if (newAchievements.isNotEmpty()) {
                achievements += newAchievements
                activeAgents += newAchievements.map(::findAgentForAchievement).onEach { it.load() }
            }
        }
    }
}