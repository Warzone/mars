package network.warzone.mars.player.achievements.variants

import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class TotalKillsAchievement(
    val params: AgentParams.TotalKillsAgentParams,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        //sendDebugMessage("onProfileUpdate called by TotalKillsAchievement")
        //sendDebugMessage("event.update.reason.name = " + event.update.reason.name)
        if (event.update.reason != PlayerUpdateReason.KILL) return
        val killerProfile = event.update.updated
        if (killerProfile.stats.kills >= this.params.targetKills) {
            emitter.emit(killerProfile)
        }
    }
}