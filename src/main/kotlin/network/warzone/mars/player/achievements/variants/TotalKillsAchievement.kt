package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.hasMode
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

class TotalKillsAchievement(
    val params: AgentParams.TotalKillsAgentParams,
    override val emitter: AchievementEmitter
) : AchievementAgent, Listener {
    @EventHandler
    fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
        val killer = event.killer ?: return@runBlocking
        val killerPlayer = killer.player.orElse(null)?.player?.bukkit ?: return@runBlocking
        val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
        val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking;

        if (profile.stats.kills >= params.targetKills) {
            emitter.emit(killerPlayer)
        }
    }
}