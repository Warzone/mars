package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementParent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

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
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val profile = context.getPlayerProfile()

                if (profile.stats.records.longestProjectileKill?.distance == bowDistance) {
                    //AchievementEmitter.emit(killer.player.get(), achievement)
                }
            }

            override fun unload() {
                //HandlerList.unregisterAll(this)
            }
        }
}