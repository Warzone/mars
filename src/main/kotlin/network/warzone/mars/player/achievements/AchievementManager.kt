package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.models.AchievementParent
import network.warzone.mars.player.achievements.variants.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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
                KillstreakAchievement(agentParams, emitter)
            }
            is AgentParams.TotalKillsAgentParams -> {
                TotalKillsAchievement(agentParams, emitter)
            }
            is AgentParams.FireDeathAgentParams -> {
                FireDeathAchievement(agentParams, emitter)
            }
            is AgentParams.CaptureNoSprintAgentParams -> {
                CaptureNoSprintAchievement(emitter)
            }
            //TODO: Refactor all single-parameter achievements to just
            // pass the parameter, like how it's being done here.
            is AgentParams.ChatMessageAgentParams -> {
                ChatMessageAchievement(agentParams.message, emitter)
            }
            is AgentParams.LevelUpAgentParams -> {
                LevelUpAchievement(agentParams.level, emitter)
            }
            is AgentParams.CaptureWoolAgentParams -> {
                CaptureWoolAchievement(agentParams.captures, emitter)
            }
            // ...
            else -> throw IllegalArgumentException("Unknown AgentParams for achievement ${achievement.name}")
        }
    }

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

                sendDebugMessage("Achievements updated via fetchNewAchievements()")
                sendConsoleMessage("Achievements updated via fetchNewAchievements()")
                sendConsoleMessage("New Achievements added: ")
                newAchievements.forEach { sendConsoleMessage("- ${it.name}")}
                sendConsoleMessage("Current Achievements: ")
                currentAchievements.forEach { sendConsoleMessage("- ${it.name}")}
                sendConsoleMessage("Existing database achievements: ")
                AchievementFeature.printAchievements()
            }
        }
    }
}
/**

























    val debugPrefix: String = ChatColor.DARK_GRAY.toString() + "[@] " + ChatColor.GRAY.toString()
    val MewTwoKing: Player?
        get() = Bukkit.getPlayer("MewTwoKing")
    private var initializedAgents = false
    private var initializedParentAgents = false
    val achievementAgents: MutableList<AchievementAgent> = mutableListOf()
    val achievementParentAgents: MutableMap<AchievementParentAgent, MutableList<AchievementAgent>> = mutableMapOf()

    // TODO: Achievement parents for the GUI are currently initialized once a player joins for the first time.
    //  This may need to be changed to a different event.
    @EventHandler
    private fun onMatchStart(event: MatchStartEvent) {
        sendDebugMessage("AchievementManager.onMatchStart called")
        if (!initializedAgents) {
            initializeAgents()
        }
        for (agent in achievementAgents) agent.match = event.match
        if (!initializedParentAgents) {
            initializeParentAgents()
        }
        sendDebugMessage("AchievementManager.onMatchStart finished")
    }

    //TODO: Instead of using Achievement.values(), we will be using whatever
    // is in the API database.
    private fun initializeAgents() {
        for (achievement in Achievement.values()) {
            println("Enabling achievement: $achievement")
            val agent = achievement.agentProvider()
            //if (!isValidGamemode(agent.gamemode)) return;
            agent.load()
            achievementAgents += agent
        }
        initializedAgents = true
    }

    private fun initializeParentAgents() {
        sendDebugMessage("AchievementManager.initializeParentAgents() called")
        for (achievement in Achievement.values()) {
            val parent = achievement.agentProvider().parent
            achievementParentAgents.getOrPut(parent.agent) { mutableListOf() }.add(achievement.agentProvider())
        }
        initializedParentAgents = true
        sendDebugMessage("AchievementManager.initializeParentAgents() finished")
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

    fun sendDebugMessage(message: String) {
        MewTwoKing?.sendMessage(debugPrefix + message)
    }

    fun sendDebugStartMessage(achievement: Achievement, functionName: String) {
        MewTwoKing?.sendMessage(debugPrefix + "\\u00A7e" + achievement.name + "." + functionName + " started.")
    }

    fun sendDebugFinishMessage(achievement: Achievement, functionName: String) {
        MewTwoKing?.sendMessage(debugPrefix + "\\u00A7e" + achievement.name + "." + functionName + " finished.")
    }
}**/