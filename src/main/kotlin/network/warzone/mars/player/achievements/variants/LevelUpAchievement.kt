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
        //TODO: If a player has already passed the target level, they can never obtain
        // this achievement as long as "==" is used instead of ">=". However, using
        // the latter operation would cause excessive profile fetching every time
        // a player levels up.
        if (event.data.level >= level) {
            emitter.emit(event.data.player)
        }
    }
}