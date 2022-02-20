package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.commands.providers.PlayerName
import network.warzone.mars.utils.audience
import network.warzone.mars.utils.matchPlayer
import network.warzone.mars.utils.translate
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.event.ClickEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import javax.annotation.Nullable

class MiscCommands {
    fun onAppealLink(@Sender sender: Player) {
        val appealLink = Mars.get().config.getString("server.links.appeal") ?: translate("command.misc.appeal.error", sender)
        sender.matchPlayer.sendMessage(
            translatable("command.misc.appeal.output", NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(appealLink))
        )
    }

    fun onRulesLink(@Sender sender: Player) {
        val rulesLink = Mars.get().config.getString("server.links.rules") ?: "No rules link available"
        sender.matchPlayer.sendMessage(
            translatable("command.misc.rules.output", NamedTextColor.YELLOW, text(rulesLink, NamedTextColor.GOLD))
        )
    }

    @Command(aliases = ["gmc"], desc = "Set your gamemode to Creative", perms = ["mars.gmc"])
    fun onCreativeSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.CREATIVE
        sender.sendMessage("${ChatColor.AQUA}Gamemode updated")
    }

    @Command(aliases = ["gms"], desc = "Set your gamemode to Survival", perms = ["mars.gms"])
    fun onSurvivalSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.SURVIVAL
        sender.sendMessage("${ChatColor.AQUA}Gamemode updated")
    }

    @Command(aliases = ["gmsp"], desc = "Set your gamemode to Spectator", perms = ["mars.gmsp"])
    fun onSpectatorSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.SPECTATOR
        sender.sendMessage("${ChatColor.AQUA}Gamemode updated")
    }

    @Command(aliases = ["ping"], desc = "View a player's ping", usage = "[player]")
    fun onPingView(@Sender sender: CommandSender, @Nullable @PlayerName playerName: String?) {
        val player = playerName?.let { Bukkit.getPlayer(it) } ?: sender
        if (player !is Player) throw CommandException("Consoles cannot check their own pings")

        val ping = player.spigot().ping
        val pingComponent = translatable("misc.ping", NamedTextColor.AQUA, text(ping))
        if (player == sender) sender.audience.sendMessage(
            translatable("command.misc.ping.self", NamedTextColor.GRAY,
                pingComponent
            ))
        else sender.audience.sendMessage(
            translatable("command.misc.ping.other", NamedTextColor.GRAY,
                text(player.name, NamedTextColor.AQUA),
                pingComponent
            ))
    }
}