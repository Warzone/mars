package network.warzone.mars.player.achievements.variants.losses

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
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.goals.events.GoalCompleteEvent
import tc.oc.pgm.goals.events.GoalStatusChangeEvent
import tc.oc.pgm.goals.events.GoalTouchEvent

//TODO: Currently, this achievement assumes a map has exactly 10 wools to break per team.
// Will need to adjust this in the final implementation.
// -
// Perhaps it could also account for DTW maps with more than two teams, but that's probably
// less important atm since I don't know of any DTW maps like this.
object PinchOfSaltAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Lose a DTW match with only one enemy wool left to break."
            override val gamemode: String = "DTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                //Mars.registerEvents(this)
            }

            private var brokenWools = mutableMapOf<Competitor, Int>()
            private var totalWools: Int = 10

            //TODO: Fix errors with this event
            @EventHandler
            fun onWoolBreak(event: GoalStatusChangeEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onGoalStatusChange() called")
                if (!event.match.hasMode(Gamemode.DESTROY_THE_MONUMENT)) return@runBlocking

                val woolCount = brokenWools.getOrDefault(event.competitor, 0)
                brokenWools[event.competitor!!] = woolCount + 1

                AchievementManager.sendDebugMessage(achievement.name + ".onGoalStatusChange() finished")
            }

            @EventHandler
            fun onMatchEnd(event: MatchFinishEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onMatchEnd() called")
                if (!event.match.hasMode(Gamemode.DESTROY_THE_MONUMENT)) return@runBlocking

                val matchParticipants: MutableList<MatchPlayer> = event.match.participants.toMutableList()
                val matchWinners: MutableList<MatchPlayer> = event.winners.flatMap { it.players }.toMutableList()
                if (matchParticipants.size == matchWinners.size) return@runBlocking //The match was a tie.
                val matchLosers: MutableList<MatchPlayer> = matchParticipants.toSet().subtract(matchWinners.toSet()).toMutableList()

                // Loop through losers
                matchLosers.forEach { loser ->
                    val competitor = loser.competitor ?: return@forEach
                    val woolCount = brokenWools.getOrDefault(competitor, 0)

                    // Check if there was only one wool left to break
                    if (woolCount == totalWools - 1) {
                        // Award achievement to loser
                        val context = PlayerManager.getPlayer(loser.id) ?: return@forEach
                        val profile = PlayerFeature.fetch(context.player.name) ?: return@forEach
                        AchievementEmitter.emit(profile, loser.simple, achievement)
                    }
                }

                AchievementManager.sendDebugMessage(achievement.name + ".onMatchEnd() finished")
            }

            override fun unload() {
                //HandlerList.unregisterAll(this)
            }
    }
}