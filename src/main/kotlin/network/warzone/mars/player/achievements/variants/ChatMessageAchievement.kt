package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateData
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

// an achievement for simply saying something in chat, i.e. "gg"
class ChatMessageAchievement(
    private val message: String,
    override val emitter: AchievementEmitter
) : AchievementAgent {
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.CHAT) return
        val msg = ((event.update.data as PlayerUpdateData.ChatUpdateData).data.message)
        if (msg != message) return
        emitter.emit(event.update.updated)
    }
}