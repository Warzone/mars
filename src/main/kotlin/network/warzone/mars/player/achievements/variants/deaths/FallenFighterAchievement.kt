package network.warzone.mars.player.achievements.variants.deaths

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.match.tracker.WoolTracker
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
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.goals.events.GoalTouchEvent
import tc.oc.pgm.wool.MonumentWool
import java.util.*

//TODO: The following scenarios must be tested/addressed:
// - Upon picking up the wool, if a player manually removes it from their inventory (ex: by dropping
//   or placing it), the achievement will still count despite the player not possessing the wool
//   objective in their inventory.
// - If a player leaves the match before dying with the wool, they can earn the achievement by
//   rejoining the match and dying.
object FallenFighterAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Die while carrying a wool objective in CTW."
            override val gamemode: String = "CTW"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            // Keep track of players who picked up wool
            private val woolCarriers = mutableSetOf<UUID>()

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler
            fun onWoolPickup(event: GoalTouchEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onWoolPickup called")
                if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return@runBlocking
                if (event.goal !is MonumentWool) return@runBlocking
                val player = event.player ?: return@runBlocking
                woolCarriers.add(player.id)
                AchievementManager.sendDebugMessage(achievement.name + ".onWoolPickup finished")
            }

            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath called")
                val victim = event.victim
                if (woolCarriers.contains(victim.id)) {
                    // The player died while carrying the wool
                    val context = PlayerManager.getPlayer(victim.id) ?: return@runBlocking
                    val profile = PlayerFeature.fetch(context.player.name) ?: return@runBlocking
                    AchievementEmitter.emit(profile, victim.simple, achievement)
                    woolCarriers.remove(victim.id) // Remove the player from the wool carriers
                }
                AchievementManager.sendDebugMessage(achievement.name + ".onPlayerDeath finished")
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}