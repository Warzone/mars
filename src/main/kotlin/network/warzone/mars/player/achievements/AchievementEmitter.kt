package network.warzone.mars.player.achievements

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.models.PlayerProfile

object AchievementEmitter {
    fun emit(profile: PlayerProfile, player: SimplePlayer, achievement: Achievement) {
        println("emit function called by " + achievement.name)

        if (profile.stats.achievements.contains(achievement.name)) return;
        PlayerManager.getPlayer(player.id)?.player?.sendMessage("You've earned an achievement: " + achievement.name);

        println("Achievement " + achievement.agentProvider().title + " earned by " + player.name);


        ApiClient.emit(
            OutboundEvent.PlayerAchievement, PlayerAchievementData(
            player,
            achievement.name
            )
        )

    }
}