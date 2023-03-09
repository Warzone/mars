package network.warzone.mars.achievement

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.player.achievements.GamemodeEnum
import network.warzone.mars.player.achievements.InvalidGamemodeException
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.MatchPlayer

interface AchievementAgent {
    var match: Match?;
    val title: String
    val description: String
    val gamemode: String

    fun load()
    fun unload()
}


object AchievementEmitter {
    fun emit(player: MatchPlayer, achievement: Achievement) {
        if (achievement.isComplete) return

        player.bukkit.sendMessage("You've earned an achievement: ${achievement.agentProvider().title}!")
        // otherwise notify the player of the achievement and publish to the API
        println("Achievement " + achievement.agentProvider().title + " earned by " + player.name.toString());

        ApiClient.emit(OutboundEvent.PlayerAchievement, PlayerAchievementData(
            achievement,
            true
        ))
        achievement.isComplete = true;

    }
}

fun isValidGamemode(gamemode: String) : Boolean {
    try {
        GamemodeEnum.valueOf(gamemode.toUpperCase())
        return true
    } catch (e: IllegalArgumentException) {
        throw InvalidGamemodeException(gamemode)
    }
}