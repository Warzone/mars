package network.warzone.mars.player.achievements.variants.losses

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.player.MatchPlayer

object AllForNothingAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Lose any match lasting over 30 minutes."
            override val gamemode: String = "NONE"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onMatchEnd(event: MatchFinishEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onMatchEnd() called")
                val matchDuration = event.match.duration.toMinutes()
                if (matchDuration < 30) return@runBlocking //The match did not last 30+ minutes.

                val matchParticipants: MutableList<MatchPlayer> = event.match.participants.toMutableList()
                val matchWinners: MutableList<MatchPlayer> = event.winners.flatMap { it.players }.toMutableList()
                if (matchParticipants.size == matchWinners.size) return@runBlocking //The match was a tie.

                val matchLosers: MutableList<MatchPlayer> = matchParticipants.toSet().subtract(matchWinners.toSet()).toMutableList()
                matchLosers.forEach { loser ->
                    val context = PlayerManager.getPlayer(loser.id) ?: return@forEach
                    val profile = PlayerFeature.fetch(context.player.name) ?: return@forEach
                    AchievementEmitter.emit(profile, loser.simple, achievement)
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onMatchEnd() finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}