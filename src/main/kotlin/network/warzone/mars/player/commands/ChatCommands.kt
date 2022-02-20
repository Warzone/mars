package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.utils.audience
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

class ChatCommands {
    @Command(aliases = ["mute", "toggle"], desc = "Mute/unmute the chat", perms = ["mars.chat.mute"])
    fun onChatMute(@Sender sender: CommandSender) {
        val isEnabled = Mars.get().config.getBoolean("chat.enabled")
        if (isEnabled) Bukkit.getOnlinePlayers().forEach { player ->
            player.audience.sendMessage(translatable("command.chat.mute.on", NamedTextColor.AQUA, text(sender.name)))
        }
        else Bukkit.getOnlinePlayers().forEach { player ->
            player.audience.sendMessage(translatable("command.chat.mute.off", NamedTextColor.AQUA, text(sender.name)))
        }
        Mars.get().config.set("chat.enabled", !isEnabled)
        Mars.get().saveConfig()
    }

    @Command(aliases = ["clear"], desc = "Clear the chat", perms = ["mars.chat.clear"])
    fun onChatClear(@Sender sender: CommandSender) {
        Bukkit.getOnlinePlayers().forEach { player ->
            repeat(200) {
                player.sendMessage("\n")
            }
            player.audience.sendMessage(translatable("command.chat.clear", NamedTextColor.AQUA, text(sender.name)))
        }
    }
}