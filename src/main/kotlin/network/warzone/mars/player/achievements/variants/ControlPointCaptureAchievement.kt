package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class ControlPointCaptureAchievement(
    val captures: Int,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.CONTROL_POINT_CAPTURE) return
        val profile = event.update.updated
        if (profile.stats.objectives.controlPointCaptures >= captures) {
            emitter.emit(event.update.updated.name)
        }
    }
}