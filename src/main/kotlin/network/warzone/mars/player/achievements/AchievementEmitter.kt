package network.warzone.mars.player.achievements

import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AchievementStatistic
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import org.bukkit.Bukkit
import java.util.*


// A class for adding completed achievements to a player's profile.
class AchievementEmitter(private val achievement: Achievement) {

    companion object {
        val LEVEL_UP_SOUND = sound(key("entity.player.levelup"), Sound.Source.MASTER, 1f, 1f)
    }

    // Add an achievement to the specified profile.
    fun emit(playerName: String) {
        val profile: PlayerProfile = PlayerFeature.getCached(playerName) ?: return
        if (profile.stats.achievements.containsKey(achievement._id.toString())) return // Player already has achievement.

        // Print achievement earn to player and console.
        val player = Bukkit.getPlayer(profile._id)
        if (player != null) {
            val audience = AUDIENCE_PROVIDER.player(player)
            val hoverText = text(achievement.description, NamedTextColor.GOLD)
            val achievementComponent = text("You've earned an achievement: ", NamedTextColor.GRAY)
                .append(text(achievement.name, NamedTextColor.AQUA))
                .append(text("!", NamedTextColor.GRAY))
                .hoverEvent(hoverText)
            audience.sendMessage(achievementComponent)
            audience.playSound(LEVEL_UP_SOUND)
        }

        val completionTime = Date().time

        val data = PlayerAchievementData(
            SimplePlayer(profile._id, profile.name),
            achievement._id,
            completionTime
        )

        if (achievement.firstCompletion == null) {
            achievement.firstCompletion = profile._id
            val broadcast = Component.newline()
                .append(text(player.name, NamedTextColor.GOLD))
                .append(text(" is the first to complete the achievement ", NamedTextColor.GRAY))
                .append(
                    text("\"${achievement.name}\"", NamedTextColor.AQUA)
                        .hoverEvent(
                            text(achievement.description).color(NamedTextColor.GOLD)
                        )
                )
                .append(text("!", NamedTextColor.GRAY))
                .append(Component.newline())
            AUDIENCE_PROVIDER.all().sendMessage(broadcast)
        }

        profile.stats.achievements[achievement._id.toString()] = AchievementStatistic(completionTime)

        // Emit achievement completion to the database.
        ApiClient.emit(OutboundEvent.PlayerAchievement, data)
    }
}