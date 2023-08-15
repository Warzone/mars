package network.warzone.api.database.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import network.warzone.mars.feature.NamedResource
import network.warzone.mars.player.achievements.models.AchievementParent
import java.util.*

enum class AgentType {
    TOTAL_KILLS_AGENT,
    KILL_STREAK_AGENT,
    FIRE_DEATH_AGENT,
    CHAT_MESSAGE_AGENT,
    LEVEL_UP_AGENT,
    CAPTURE_WOOL_AGENT,
    //COMPOSITE_AGENT
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
    @SerialName("FireDeathAgentParams")
    object FireDeathAgentParams : AgentParams()

    @Serializable
    @SerialName("CaptureNoSprintAgentParams")
    object CaptureNoSprintAgentParams : AgentParams()

    @Serializable
    @SerialName("CompositeAgentParams")
    data class CompositeAgentParams(val agents: List<Agent>) : AgentParams()

    @Serializable
    @SerialName("ChatMessageAgentParams")
    data class ChatMessageAgentParams(val message: String) : AgentParams()

    @Serializable
    @SerialName("LevelUpAgentParams")
    data class LevelUpAgentParams(val level: Int) : AgentParams()

    @Serializable
    @SerialName("WoolCapturesAgentParams")
    data class WoolCapturesAgentParams(val captures: Int) : AgentParams()

    @Serializable
    @SerialName("FirstBloodAgentParams")
    data class FirstBloodAgentParams(val target: Int) : AgentParams()
    // add more classes as needed for each type of parameter set

    @Serializable
    @SerialName("BowDistanceAgentParams")
    data class BowDistanceAgentParams(val distance: Long) : AgentParams()

    @Serializable
    @SerialName("FlagCapturesAgentParams")
    data class FlagCapturesAgentParams(val captures: Int) : AgentParams()

    @Serializable
    @SerialName("FlagDefendsAgentParams")
    data class FlagDefendsAgentParams(val defends: Int) : AgentParams()

    @Serializable
    @SerialName("WoolDefendsAgentParams")
    data class WoolDefendsAgentParams(val defends: Int) : AgentParams()
}

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
    @Serializable
    val parent: AchievementParent? = null,
    val agent: Agent,
) : NamedResource {

    // Structural equality of _id's
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Achievement

        if (_id != other._id) return false

        return true
    }

    override fun hashCode(): Int {
        return _id.hashCode()
    }
}