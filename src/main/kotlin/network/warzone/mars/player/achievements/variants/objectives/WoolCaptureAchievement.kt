package network.warzone.mars.player.achievements.variants.objectives

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
import tc.oc.pgm.api.match.event.MatchEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.api.player.event.MatchPlayerEvent
import tc.oc.pgm.destroyable.DestroyableDestroyedEvent
import tc.oc.pgm.destroyable.DestroyableEvent
import tc.oc.pgm.wool.PlayerWoolPlaceEvent

object WoolCaptureAchievement {
    fun createAchievement(targetCaptures: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Capture a total of ${targetCaptures} wools in CTW games."
            override val gamemode: String = "CTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onWoolCapture(event: PlayerWoolPlaceEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onWoolCapture called")
                val matchPlayer = event.player.player.get()
                val context = PlayerManager.getPlayer(matchPlayer.id) ?: return@runBlocking
                //Note: I tried context.getPlayerProfile(), but it seems the wool captures
                // statistic is not updated on the server-side til the player relogs.
                val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking
                val woolCaptures = profile.stats.objectives.woolCaptures + 1 //+1 to account for this event being called before the stat is incremented

                AchievementManager.sendDebugMessage("woolCaptures = $woolCaptures")
                if (woolCaptures >= targetCaptures) {
                    AchievementEmitter.emit(profile, matchPlayer.simple, achievement)
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onWoolCapture finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}