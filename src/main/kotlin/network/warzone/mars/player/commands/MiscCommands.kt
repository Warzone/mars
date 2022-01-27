package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.match.tracker.KillstreakTracker
import network.warzone.mars.utils.matchPlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.event.ClickEvent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import javax.annotation.Nullable

class MiscCommands {
    fun onAppealLink(@Sender sender: Player) {
        val appealLink = Mars.get().config.getString("server.links.appeal") ?: "No appeal link available"
        sender.matchPlayer.sendMessage(
            Component.text("Click here to appeal a punishment", NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(appealLink))
        )
    }

    fun onRulesLink(@Sender sender: Player) {
        val rulesLink = Mars.get().config.getString("server.links.rules") ?: "No rules link available"
        sender.sendMessage("${ChatColor.YELLOW}Please read and abide by our server rules which can be found at ${ChatColor.GOLD}$rulesLink")
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

    @Command(aliases = ["killstreak", "ks"], desc = "View your current killstreak", usage = "[player]")
    fun onKillstreakView(@Sender sender: Player, @Nullable playerName: String?) {
        val player = playerName?.let { Bukkit.getPlayer(it) } ?: sender
        val killstreak = KillstreakTracker.getKillstreak(player.uniqueId ?: sender.uniqueId)
            ?: return sender.sendMessage("${ChatColor.RED}Killstreaks are not being tracked")
        val (_, color) = KillstreakTracker.getNearestTrackedKillstreak(killstreak)
        if (killstreak > 0) {
            val perspective = if (player == sender) "You're" else "${player.name} is"
            sender.matchPlayer.sendMessage(
                Component
                    .text("$perspective on a killstreak of ", NamedTextColor.GREEN)
                    .append(Component.text(killstreak, color, TextDecoration.BOLD))
                    .append(Component.text(" kills.", NamedTextColor.GREEN))
            )
        } else {
            val perspective = if (player == sender) "You don't" else "${player.name} doesn't"
            sender.sendMessage("${ChatColor.RED}$perspective have a killstreak yet.")
        }
    }

    @Command(aliases = ["ping"], desc = "View a player's ping", usage = "[player]")
    fun onPingView(@Sender sender: CommandSender, @Nullable playerName: String?) {
        val player = playerName?.let { Bukkit.getPlayer(it) } ?: sender
        if (player !is Player) throw CommandException("Consoles cannot check their own pings")

        val possessive = "${player.name}${ChatColor.GRAY}${if (player.name.endsWith("s")) "'" else "'s"}"
        val ping = player.spigot().ping
        if (player == sender) sender.sendMessage("${ChatColor.GRAY}Your ping is ${ChatColor.AQUA}${ping}ms")
        else sender.sendMessage("${ChatColor.AQUA}$possessive ${ChatColor.GRAY}ping is ${ChatColor.AQUA}${ping}ms")
    }
}