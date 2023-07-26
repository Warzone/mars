package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.matchPlayer
import network.warzone.mars.utils.simple
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import java.util.*

// TODO: This currently just emits the achievement for players who finish a match without sprinting,
//  regardless of whether they captured a wool or not.
class CaptureNoSprintAchievement(val achievement: Achievement) : AchievementAgent, Listener {
    var playersWhoSprinted: MutableList<UUID> = emptyList<UUID>().toMutableList()

    override fun onMatchFinish(event: MatchFinishEvent) = runBlocking {
        event.match.participants.forEach { matchPlayer ->
            if (matchPlayer.player?.id !in playersWhoSprinted) {
                val player = matchPlayer ?: return@forEach
                val context = PlayerManager.getPlayer(player.id) ?: return@forEach
                val profile = PlayerFeature.fetch(context.player.name) ?: return@forEach
                AchievementEmitter.emit(profile, player.simple, achievement)
            }
        }
        playersWhoSprinted.clear()
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) = runBlocking {
        playersWhoSprinted.clear()
    }

    @EventHandler
    fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) = runBlocking {
        val playerId = event.player.matchPlayer.id
        if (!playersWhoSprinted.contains(playerId)) {
            playersWhoSprinted.add(playerId)
            sendDebugMessage("Player " + event.player.name + " has sprinted.")
        }
    }
}