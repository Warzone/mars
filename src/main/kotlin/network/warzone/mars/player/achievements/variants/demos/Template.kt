package network.warzone.mars.player.achievements.variants.demos

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementParent
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object Template {
    fun createTemplateAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Insert your achievement description here"
            // TODO: Change gamemode from String to GamemodeEnum for all variants
            override val gamemode: String = "Insert the gamemode here"
            override val id: String = achievement.name
            override val parent: AchievementParent = AchievementParent.NO_PARENT

            override fun load() {
                Mars.registerEvents(this)
            }

            override fun unload() {
                HandlerList.unregisterAll(this)
            }
    }
}

//TODO: In various achievement variants, I use fetch() to fetch something from the API.
// I remember having a conversation with tank where he mentioned it wasn't good to fetch
// from the API I think. Will need to clarify this and provide a fix if needed.