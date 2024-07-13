package network.warzone.mars.player.achievements.variants

import network.warzone.mars.match.tracker.PlayerLevelUpEvent
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

class LevelUpAchievement(
    val level: Int,
    override val emitter: AchievementEmitter
) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerLevelUpEvent) {
        if (event.data.level >= level) {
            emitter.emit(event.data.player.name)
        }
    }
}