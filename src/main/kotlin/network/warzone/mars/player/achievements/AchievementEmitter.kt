package network.warzone.mars.player.achievements

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.AchievementStatistic
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*



// A class for adding completed achievements to a player's profile.
class AchievementEmitter(private val achievement: Achievement) {
    // Add an achievement to the specified profile.
    fun emit(playerName: String) {
        val profile: PlayerProfile = PlayerFeature.getCached(playerName) ?: return
        if (profile.stats.achievements.containsKey(achievement._id.toString())) return // Player already has achievement.

        // Print achievement earn to player and console.
        val player = Bukkit.getPlayer(profile._id)
        if (player != null) {
            val achievementMessage = TextComponent("${ChatColor.GRAY}You've earned an achievement: ${ChatColor.AQUA}${achievement.name}${ChatColor.GRAY}!")
            val hoverText = ComponentBuilder("${ChatColor.GOLD}${achievement.description}").create()
            achievementMessage.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
            player.playSound(player.location, Sound.LEVEL_UP, 1.0f, 1.0f)
            player.spigot().sendMessage(achievementMessage)
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
                .append(Component.text(player.name, NamedTextColor.GOLD))
                .append(Component.text(" is the first to complete the achievement ", NamedTextColor.GRAY))
                .append(
                    Component.text("\"${achievement.name}\"", NamedTextColor.AQUA)
                        .hoverEvent(
                            Component.text(achievement.description).color(NamedTextColor.GOLD)
                        )
                )
                .append(Component.text("!", NamedTextColor.GRAY))
                .append(Component.newline())
            AUDIENCE_PROVIDER.all().sendMessage(broadcast)
        }

        profile.stats.achievements[achievement._id.toString()] = AchievementStatistic(completionTime)

        // Emit achievement completion to the database.
        ApiClient.emit(OutboundEvent.PlayerAchievement, data)
    }
}