package network.warzone.mars.player.achievements

import network.warzone.mars.Mars
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.MatchPlayer

object AchievementManager : Listener {
    init {
        Mars.registerEvents(this)
        println("Achievement Manager HAS BEEN ENABLED THIS IS ENABLED.")
    }

    private var initializedAgents = false
    private var initializedParentAgents = false
    private val achievementAgents: MutableList<AchievementAgent> = mutableListOf()
    val achievementParentAgents: MutableMap<AchievementParentAgent, MutableList<AchievementAgent>> = mutableMapOf()

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        println("MEWTWOKING HAS JOINED THE SERVER OMG OMG OMG")
    }

    // TODO: Achievement parents for the GUI are currently initialized once a player joins for the first time.
    //  This may need to be changed to a different event.
    @EventHandler
    private fun onMatchStart(event: MatchStartEvent) {
        println("A MATCH HAS STARTED OMG OMG OMG")
        if (!initializedAgents) {
            initializeAgents()
        }
        for (agent in achievementAgents) agent.match = event.match
        if (!initializedParentAgents) {
            initializeParentAgents()
        }
    }

    //TODO: Instead of using Achievement.values(), we will be using whatever
    // is in the API database.
    private fun initializeAgents() {
        for (achievement in Achievement.values()) {
            println("Enabling achievement: $achievement")
            val agent = achievement.agentProvider()
            if (!isValidGamemode(agent.gamemode)) return;
            agent.load()
            achievementAgents += agent
        }
        initializedAgents = true
    }

    private fun initializeParentAgents() {
        for (achievement in Achievement.values()) {
            val parent = achievement.agentProvider().parent
            achievementParentAgents.getOrPut(parent.agent) { mutableListOf() }.add(achievement.agentProvider())
        }
        initializedParentAgents = true
    }

    fun unload() {
        HandlerList.unregisterAll(this)
        for (agent in achievementAgents) agent.unload()
    }

    fun isValidGamemode(gamemode: String) : Boolean {
        try {
            GamemodeEnum.valueOf(gamemode.toUpperCase())
            return true
        }
        catch (e: IllegalArgumentException) {
            throw InvalidGamemodeException(gamemode)
        }
    }
}