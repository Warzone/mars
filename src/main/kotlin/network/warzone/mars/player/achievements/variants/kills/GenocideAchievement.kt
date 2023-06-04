package network.warzone.mars.player.achievements.variants.kills

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.*
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.simple
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object GenocideAchievement {
    fun createGenocideAchievement(targetKills: Int, achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Kill ${targetKills} players."
            override val gamemode: String = "NONE"
            override val parent: AchievementParent = AchievementParent.PATH_TO_GENOCIDE
            override val id: String = achievement.name

            override fun load() {
                Mars.registerEvents(this)
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                println("onPlayerDeath triggered by " + achievement.name)
                val killer = event.killer ?: return@runBlocking
                val context = PlayerManager.getPlayer(killer.id) ?: return@runBlocking
                val playerName = context.player.name.toString();
                val profile = PlayerFeature.fetch(playerName);

                println("    profile.stats.kills = " + profile?.stats?.kills)
                println("    targetKIlls = " + targetKills)

                if (profile != null) {
                    if (profile.stats.kills >= targetKills) {
                        AchievementEmitter.emit(profile, killer.player.get().simple, achievement)
                    }
                }
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
        }
}