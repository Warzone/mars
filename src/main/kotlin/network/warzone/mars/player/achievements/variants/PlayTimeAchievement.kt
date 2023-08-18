package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.match.MatchManager
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler
import tc.oc.pgm.api.match.event.MatchFinishEvent

class PlayTimeAchievement(
    val hours: Long,
    override val emitter: AchievementEmitter
) : AchievementAgent {

    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.MATCH_END) return

        val playerProfile = event.update.updated
        val targetMillis = hours * 60 * 60 * 1000 // Convert hours to milliseconds

        if (playerProfile.stats.gamePlaytime >= targetMillis) {
            emitter.emit(playerProfile)
        }
    }
}