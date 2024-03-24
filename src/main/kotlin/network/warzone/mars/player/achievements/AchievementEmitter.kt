package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*


// A class for adding completed achievements to a player's profile.
class AchievementEmitter(private val achievement: Achievement) {
    // Fetch a player profile as needed.
    fun emit(player: Player) {
        Mars.async {
            val profile = PlayerFeature.fetch(player.name)!!
            emit(profile)
        }
    }

    // Add an achievement to the specified profile.
    fun emit(profile: PlayerProfile) {
        if (profile.stats.achievements.containsKey(achievement._id.toString())) return // Player already has achievement.

        // Print achievement earn to player and console.
        val player = Bukkit.getPlayer(profile._id)
        if (player != null) {
            player.sendMessage("${ChatColor.GRAY}You've earned an achievement: ${ChatColor.AQUA}" + achievement.name + "${ChatColor.GRAY}!")
            println("Achievement " + achievement.name + " earned by " + player.name)
        }

        // Emit achievement completion to the database.
        ApiClient.emit(
            OutboundEvent.PlayerAchievement, PlayerAchievementData(
                SimplePlayer(profile._id, profile.name),
                achievement._id,
                Date().time
            )
        )
    }
}