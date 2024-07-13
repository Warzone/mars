package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.mars.api.socket.models.PlayerUpdateEvent
import network.warzone.mars.api.socket.models.PlayerUpdateReason
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.utils.matchPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSprintEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import java.util.*

// TODO: This currently just emits the achievement for players who finish a match without sprinting,
//  regardless of whether they captured a wool or not.
class CaptureNoSprintAchievement(override val emitter: AchievementEmitter) : AchievementAgent, Listener {
    var playersWhoSprinted: MutableList<UUID> = emptyList<UUID>().toMutableList()

    @EventHandler
    fun onMatchFinish(event: MatchFinishEvent) = runBlocking {
        event.match.participants.forEach { matchPlayer ->
            if (matchPlayer.player?.id !in playersWhoSprinted) {
                val player = matchPlayer ?: return@forEach
                emitter.emit(player.bukkit.name)
            }
        }
        playersWhoSprinted.clear()
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) = runBlocking {
        playersWhoSprinted.clear()
    }

    @EventHandler
    fun onProfileUpdate(event: PlayerUpdateEvent) {
        if (event.update.reason != PlayerUpdateReason.WOOL_PICKUP) return
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) = runBlocking {
        val playerId = event.player.matchPlayer.id
        if (!playersWhoSprinted.contains(playerId)) {
            playersWhoSprinted.add(playerId)
        }
    }
}