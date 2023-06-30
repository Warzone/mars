package network.warzone.mars.player.achievements

import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerAchievementData
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.models.PlayerProfile

object AchievementEmitter {
    fun emit(profile: PlayerProfile, player: SimplePlayer, achievement: Achievement) {

        //TODO: Remove this later.
        //-----------------------------------
        val msg1 = TextComponent("Achievement emission called by: ")
        msg1.color = ChatColor.GREEN

        val msg2 = TextComponent(achievement.name)
        msg2.color = ChatColor.GOLD

        msg1.addExtra(msg2)

        PlayerManager.getPlayer(player.id)?.player?.spigot()?.sendMessage(msg1)
            ?: println("Error: Could not notify player of an achievement emission")
        //----------------------------------------

        if (profile.stats.achievements.contains(achievement.name)) return;

        PlayerManager.getPlayer(player.id)?.player?.sendMessage("You've earned an achievement: " + achievement.name)
            ?: println("Error: Could not notify player of achievement");

        println("Achievement " + achievement.agentProvider().title + " earned by " + player.name);


        ApiClient.emit(
            OutboundEvent.PlayerAchievement, PlayerAchievementData(
            player,
            achievement.name
            )
        )

    }
}