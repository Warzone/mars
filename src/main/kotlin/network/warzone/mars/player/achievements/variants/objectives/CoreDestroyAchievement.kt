package network.warzone.mars.player.achievements.variants.objectives

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.hasMode
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.goals.events.GoalCompleteEvent
import tc.oc.pgm.goals.events.GoalStatusChangeEvent
import tc.oc.pgm.goals.events.GoalTouchEvent

object CoreDestroyAchievement {
    fun createAchievement(targetCoreLeaks: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Destroy a core"
            override val gamemode: String = "DTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onGoalComplete(event: GoalCompleteEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onGoalComplete() called")
                if (!event.match.hasMode(Gamemode.DESTROY_THE_CORE)) return@runBlocking

                event.contributions.forEach{
                    val matchPlayer = it.playerState.player.get()
                    val context = PlayerManager.getPlayer(matchPlayer.id) ?: return@runBlocking
                    val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking

                    if (profile.stats.objectives.coreLeaks >= targetCoreLeaks) {
                        AchievementEmitter.emit(profile, matchPlayer.simple, achievement)
                    }
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onCoreBreak() finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}