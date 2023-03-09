package network.warzone.mars.achievement

import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.GamemodeEnum
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.event.MatchStartEvent

object AchievementManager : Listener {
    init {
        Mars.registerEvents(this)
    }

    private var initializedAgents = false
    private val achievementAgents: MutableList<AchievementAgent> = mutableListOf()

    @EventHandler
    private fun onMatchStart(event: MatchStartEvent) {
        if (!initializedAgents) {
            this.initializeAgents()
        }
        for (agent in achievementAgents) agent.match = event.match
    }

    private fun initializeAgents() {
        for (achievement in Achievement.values()) {
            println("Enabling achievement: $achievement")
            val agent = achievement.agentProvider()
            if (!isValidGamemode(agent.gamemode)) return;
            agent.load()
            achievementAgents += agent
        }
        this.initializedAgents = true
    }

    fun unload() {
        HandlerList.unregisterAll(this)
        for (agent in achievementAgents) agent.unload()
    }
}