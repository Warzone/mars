package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

//TODO: Ask tank about this later.
class LevelUpAchievement(
    val level: Int,
    override val emitter: AchievementEmitter
) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        val xpData = event.update.data as PlayerUpdateData.LevelUpUpdateData
        val playerProfile = event.update.updated
        if (xpData.data.level == level) {
            emitter.emit(playerProfile)
        }
    }
}