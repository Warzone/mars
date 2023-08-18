package network.warzone.mars.player.achievements.variants

import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import org.bukkit.event.EventHandler

//TODO: Doesn't work
// .
// .
// According to my testing, a "wool defend" event is only fired after killing someone who
// triggers a "wool pickup" event. When a player triggers a "wool pickup" event for wool
// objective 'a', it cannot be triggered on objective 'a' again for the rest of the match,
// because there is no event that marks objective 'a' as "unpicked-up".
class WoolDefendAchievement(
    val defends: Int,
    override val emitter: AchievementEmitter) : AchievementAgent
{
    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.WOOL_DEFEND) return
        val playerProfile = event.update.updated
        if (playerProfile.stats.objectives.woolDefends >= defends) {
            emitter.emit(playerProfile)
        }
    }
}