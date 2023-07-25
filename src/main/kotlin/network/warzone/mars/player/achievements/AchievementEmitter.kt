package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.models.PlayerProfile

object AchievementEmitter {
    fun emit(profile: PlayerProfile, player: SimplePlayer, achievement: Achievement) {
        // Check if player already has achievement.
        if (profile.stats.achievements.contains(achievement.name)) return;

        // Print achievement earn to player and console.
        PlayerManager.getPlayer(player.id)?.player?.sendMessage("You've earned an achievement: " + achievement.name)
            ?: println("Error: Could not notify player of achievement");
        println("Achievement " + achievement.name + " earned by " + player.name);

        // Emit achievement completion to the database.
        ApiClient.emit(
            OutboundEvent.PlayerAchievement, PlayerAchievementData(
            player,
            achievement.name
            )
        )
    }
}