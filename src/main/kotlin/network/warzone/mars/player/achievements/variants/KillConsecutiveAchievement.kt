package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.Agent
import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.models.PlayerProfile
import org.bukkit.event.EventHandler
import tc.oc.pgm.api.match.event.MatchFinishEvent
import java.util.*

class KillConsecutiveAchievement(
    val params: Agent.KillConsecutiveAgentParams,
    override val emitter: AchievementEmitter
) : AchievementAgent {

    private val playerKillsTimestamps = mutableMapOf<UUID, MutableList<Long>>()

    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.KILL) return

        val killerData = event.update.data as PlayerUpdateData.KillUpdateData
        val killerId = killerData.data.attacker?.id ?: return
        val profile = event.update.updated
        val currentTime = System.currentTimeMillis()

        // Update player's kill timestamps
        val timestamps = playerKillsTimestamps.computeIfAbsent(killerId) { mutableListOf() }
        timestamps.add(currentTime)

        // Check for achievement only if they have enough kills
        if (timestamps.size >= params.kills) {
            checkForAchievement(killerId, profile)
        }
    }

    private fun checkForAchievement(playerId: UUID, profile: PlayerProfile) {
        val timestamps = playerKillsTimestamps[playerId] ?: return

        // Last x kill timestamps
        val recentKillsTimestamps = timestamps.takeLast(params.kills)

        if (params.allWithin) {
            val timeDifference = recentKillsTimestamps.last() - recentKillsTimestamps.first()
            // All kills must be within y seconds
            if (timeDifference <= params.seconds * 1000) {
                emitter.emit(profile.name)
            }
        } else {
            // Check each consecutive pair to ensure all kills are within the timeframe
            for (i in 1 until recentKillsTimestamps.size) {
                if (recentKillsTimestamps[i] - recentKillsTimestamps[i - 1] > params.seconds * 1000) {
                    return
                }
            }
            emitter.emit(profile.name)
        }
    }

    @EventHandler
    fun onMatchFinish(event: MatchFinishEvent) {
        playerKillsTimestamps.clear()
    }
}