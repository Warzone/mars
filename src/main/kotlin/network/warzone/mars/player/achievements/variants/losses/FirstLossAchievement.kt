package network.warzone.mars.player.achievements.variants.losses

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementEmitter
import network.warzone.mars.player.achievements.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.player.MatchPlayer

object FirstLossAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Lose a match for the first time."
            override val gamemode: String = "NONE"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onMatchEnd(event: MatchFinishEvent) = runBlocking {
                val matchParticipants: MutableList<MatchPlayer> = event.match.participants.toMutableList()
                matchParticipants.forEach { matchPlayer ->
                    val context = PlayerManager.getPlayer(matchPlayer.id) ?: return@forEach
                    val profile = PlayerFeature.fetch(context.player.name) ?: return@forEach;
                    if (profile.stats.losses != 0) {
                        AchievementEmitter.emit(profile, matchPlayer.simple, achievement)
                    }
                }
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
        }
    }
