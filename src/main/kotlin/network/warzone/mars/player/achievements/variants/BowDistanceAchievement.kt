package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class BowDistanceAchievement(
    val target: Long,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.KILL) return
        val killData = event.update.data as PlayerUpdateData.KillUpdateData
        sendDebugMessage("weapon: " + killData.data.weapon)
        if (killData.data.weapon != "bow") return
        val killerProfile = event.update.updated
        if (killData.data.distance!! >= target) {
            emitter.emit(killerProfile)
        }
    }
}