package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class FlagDefendAchievement(
    val defends: Int,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.FLAG_DEFEND) return
        val playerProfile = event.update.updated
        if (playerProfile.stats.objectives.flagDefends >= defends) {
            emitter.emit(event.update.updated.name)
        }
    }
}