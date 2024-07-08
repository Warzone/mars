package network.warzone.api.database.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import network.warzone.mars.feature.NamedResource
import network.warzone.mars.player.achievements.models.AchievementCategory
import java.util.*

enum class AgentType {
    TOTAL_KILLS_AGENT, /** WORKS **/
    TOTAL_DEATHS_AGENT, /** WORKS **/
    TOTAL_WINS_AGENT, /** WORKS **/
    TOTAL_LOSSES_AGENT, /** WORKS **/
    KILL_STREAK_AGENT, /** WORKS **/
    FIRE_DEATH_AGENT, /** WORKS **/
    CHAT_MESSAGE_AGENT, /** WORKS **/
    LEVEL_UP_AGENT, /** WORKS **/
    CAPTURE_NO_SPRINT_AGENT, /** DOESN'T WORK **/
    WOOL_CAPTURE_AGENT, /** WORKS **/
    FIRST_BLOOD_AGENT, /** WORKS **/
    BOW_DISTANCE_AGENT, /** WORKS **/
    FLAG_CAPTURE_AGENT, /** WORKS **/
    FLAG_DEFEND_AGENT, /** WORKS **/
    WOOL_DEFEND_AGENT, /** DOESN'T WORK **/
    MONUMENT_DAMAGE_AGENT, /** WORKS **/
    KILL_CONSECUTIVE_AGENT, /** WORKS **/
    PLAY_TIME_AGENT, /** WORKS **/
    RECORD_AGENT, /** DOESN'T WORK **/
    CONTROL_POINT_CAPTURE_AGENT, /** WORKS **/
    //COMPOSITE_AGENT
}

data class AchievementStatistic(
    val completionTime: Long
)

enum class RecordType {
    LONGEST_SESSION,
    LONGEST_PROJECTILE_KILL,
    FASTEST_WOOL_CAPTURE,
    FASTEST_FLAG_CAPTURE,
    FASTEST_FIRST_BLOOD,
    KILLS_IN_MATCH,
    DEATHS_IN_MATCH
}

@Serializable
sealed class Agent {
    @Serializable
    @SerialName("TotalKillsAgentParams")
    data class TotalKillsAgentParams(val targetKills: Int) : Agent()

    @Serializable
    @SerialName("KillStreakAgentParams")
    data class KillStreakAgentParams(val targetStreak: Int) : Agent()

    @Serializable
    @SerialName("FireDeathAgentParams")
    object FireDeathAgentParams : Agent()

    @Serializable
    @SerialName("CaptureNoSprintAgentParams")
    object CaptureNoSprintAgentParams : Agent()

    @Serializable
    @SerialName("CompositeAgentParams")
    data class CompositeAgentParams(val agents: List<Agent>) : Agent()

    @Serializable
    @SerialName("ChatMessageAgentParams")
    data class ChatMessageAgentParams(val message: String) : Agent()

    @Serializable
    @SerialName("LevelUpAgentParams")
    data class LevelUpAgentParams(val level: Int) : Agent()

    @Serializable
    @SerialName("WoolCaptureAgentParams")
    data class WoolCaptureAgentParams(val captures: Int) : Agent()

    @Serializable
    @SerialName("FirstBloodAgentParams")
    data class FirstBloodAgentParams(val target: Int) : Agent()

    @Serializable
    @SerialName("BowDistanceAgentParams")
    data class BowDistanceAgentParams(val distance: Long) : Agent()

    @Serializable
    @SerialName("FlagCaptureAgentParams")
    data class FlagCaptureAgentParams(val captures: Int) : Agent()

    @Serializable
    @SerialName("FlagDefendAgentParams")
    data class FlagDefendAgentParams(val defends: Int) : Agent()

    @Serializable
    @SerialName("WoolDefendAgentParams")
    data class WoolDefendAgentParams(val defends: Int) : Agent()

    @Serializable
    @SerialName("MonumentDamageAgentParams")
    data class MonumentDamageAgentParams(val breaks: Int) : Agent()

    @Serializable
    @SerialName("KillConsecutiveAgentParams")
    data class KillConsecutiveAgentParams(val seconds: Long, val kills: Int, val allWithin: Boolean) : Agent()

    @Serializable
    @SerialName("PlayTimeAgentParams")
    data class PlayTimeAgentParams(val hours: Long) : Agent()

    @Serializable
    @SerialName("RecordAgentParams")
    data class RecordAgentParams<T : Number>(val recordType: RecordType, val threshold: T) : Agent()

    @Serializable
    @SerialName("ControlPointCaptureAgentParams")
    data class ControlPointCaptureAgentParams(val captures: Int) : Agent()

    @Serializable
    @SerialName("TotalWinsAgentParams")
    data class TotalWinsAgentParams(val wins: Int) : Agent()

    @Serializable
    @SerialName("TotalDeathsAgentParams")
    data class TotalDeathsAgentParams(val deaths: Int) : Agent()

    @Serializable
    @SerialName("TotalLossesAgentParams")
    data class TotalLossesAgentParams(val losses: Int) : Agent()
}

data class Achievement(
    override val _id: UUID,
    override val name: String,
    val description: String,
    @Serializable
    val category: AchievementCategory? = null,
    val agent: Agent,
    var firstCompletion: UUID? = null
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