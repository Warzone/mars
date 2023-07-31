package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class AchievementEmitter(private val achievement: Achievement) {
    fun emit(player: Player) {
        Mars.async {
            val profile = PlayerFeature.fetch(player.name)!!
            emit(profile)
        }
    }

    fun emit(profile: PlayerProfile) {
        if (profile.stats.achievements.contains(achievement.name)) return

        // Print achievement earn to player and console.
        val player = Bukkit.getPlayer(profile._id)
        if (player != null) {
            player.sendMessage("You've earned an achievement: " + achievement.name)
            println("Achievement " + achievement.name + " earned by " + player.name)
        }

        // Emit achievement completion to the database.
        ApiClient.emit(
            OutboundEvent.PlayerAchievement, PlayerAchievementData(
                SimplePlayer(profile._id, profile.name),
                achievement.name
            )
        )
    }
}