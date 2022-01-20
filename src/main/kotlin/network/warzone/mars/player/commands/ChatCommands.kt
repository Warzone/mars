package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class ChatCommands {
    @Command(aliases = ["mute", "toggle"], desc = "Mute/unmute the chat", perms = ["mars.chat.mute"])
    fun onChatMute(@Sender sender: CommandSender) {
        val isEnabled = Mars.get().config.getBoolean("chat.enabled")
        if (isEnabled) Bukkit.broadcastMessage("${ChatColor.DARK_AQUA}${sender.name} muted the chat.")
        else Bukkit.broadcastMessage("${ChatColor.DARK_AQUA}${sender.name} unmuted the chat.")
        Mars.get().config.set("chat.enabled", !isEnabled)
        Mars.get().saveConfig()
    }

    @Command(aliases = ["clear"], desc = "Clear the chat", perms = ["mars.chat.clear"])
    fun onChatClear(@Sender sender: CommandSender) {
        Bukkit.getOnlinePlayers().forEach { player -> repeat(500) { player.sendMessage("\n") } }
        Bukkit.broadcastMessage("${ChatColor.DARK_AQUA}${sender.name} cleared the chat.")
    }
}