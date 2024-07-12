package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class KillstreakAchievement(
    val targetStreak: Int, override val emitter: AchievementEmitter
) : AchievementAgent {
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.KILLSTREAK) return
        val killstreakData = event.update.data as PlayerUpdateData.KillstreakUpdateData
        val killerProfile = event.update.updated
        if (killstreakData.amount == targetStreak && killerProfile.stats.killstreaks.getOrDefault(
                targetStreak, 0
            ) >= 1
        ) {
            emitter.emit(event.update.updated.name)
        }
    }
}