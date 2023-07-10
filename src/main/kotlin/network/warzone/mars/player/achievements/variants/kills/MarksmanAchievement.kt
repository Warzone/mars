package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

//TODO: According to the PlayerStats class, the record statistic is still a todo.
// Thus, I don't think this achievement can be implemented atm, since longestProjectileKill
// is part of the record statistic.
// -----------
// UPDATE: It seems the record statistic works for most of the record stats; perhaps it's just
// longestProjectileKill that is still a WIP.
object MarksmanAchievement {
    fun createMarksmanAchievement(bowDistance: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Kill a player with a bow shot from at least ${bowDistance} blocks away."
            override val gamemode: String = "NONE"
            override val parent: AchievementParent = AchievementParent.MARKSMAN
            override val id: String = achievement.name

            override fun load() {
                //Mars.registerEvents(this)
            }

            // TODO: Instead of listening for MatchPlayerDeathEvent, what if it just listens for when longestProjectileKill is updated?
            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath() called")
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking
                val bowRecord = profile.stats.records.longestProjectileKill?.distance

                AchievementManager.sendDebugMessage(achievement.name + ": bowRecord = " + bowRecord)
                if (bowRecord == bowDistance) {
                    AchievementEmitter.emit(profile, killer.player.get().simple, achievement)
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath() finished")
            }

            override fun unload() {
                //HandlerList.unregisterAll(this)
            }
        }
}