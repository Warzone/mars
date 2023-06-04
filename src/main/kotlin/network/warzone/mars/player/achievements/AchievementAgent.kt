package network.warzone.mars.player.achievements

import network.warzone.mars.player.achievements.AchievementCategory
import tc.oc.pgm.api.match.Match

interface AchievementAgent {
    var match: Match?;
    val title: String
    val description: String
    val gamemode: String
    val id: String
    val parent: AchievementParent
        get() = AchievementParent.NO_PARENT //Default parent if not overridden.


    fun load()
    fun unload()
}





























