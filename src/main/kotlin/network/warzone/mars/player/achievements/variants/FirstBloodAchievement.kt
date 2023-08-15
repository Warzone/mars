package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class FirstBloodAchievement(
    val target: Int,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.KILL) return
        val killData = event.update.data as PlayerUpdateData.KillUpdateData
        if (!killData.firstBlood) return
        val killerProfile = event.update.updated
        if (killerProfile.stats.firstBloods >= target) {
            emitter.emit(killerProfile)
        }
    }
}