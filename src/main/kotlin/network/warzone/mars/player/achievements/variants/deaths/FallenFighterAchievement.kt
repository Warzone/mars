package network.warzone.mars.player.achievements.variants.deaths

import network.warzone.mars.Mars
import network.warzone.mars.player.achievements.Achievement
import network.warzone.mars.player.achievements.AchievementAgent
import network.warzone.mars.player.achievements.AchievementParent
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match

object FallenFighterAchievement {
    fun createAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Insert your achievement description here"
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