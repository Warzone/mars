package network.warzone.api.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import network.warzone.mars.feature.NamedResource
import network.warzone.mars.feature.Resource
import java.util.*

enum class AgentType {
    TOTAL_KILLS_AGENT,
    KILL_STREAK_AGENT,
    COMPOSITE_AGENT
}

@Serializable
sealed class AgentParams {
    @Serializable
    @SerialName("TotalKillsAgentParams")
    data class TotalKillsAgentParams(val targetKills: Int) : AgentParams()

    @Serializable
    @SerialName("KillStreakAgentParams")
    data class KillStreakAgentParams(val targetStreak: Int) : AgentParams()

    @Serializable
    @SerialName("CompositeAgentParams")
    data class CompositeAgentParams(val agents: List<Agent>) : AgentParams()

    // add more classes as needed for each type of parameter set
}

//TODO: Does this class need to be serializable?
// Do any of the classes in this file need to be serializable?
@Serializable
data class Agent(
    val type: AgentType,
    @Serializable
    val params: AgentParams? = null
)

data class Achievement(
    override val _id: UUID,
    override val name: String,
    val description: String,
    val agent: Agent
) : NamedResource