package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import network.warzone.mars.commands.providers.PlayerName
import network.warzone.mars.match.models.PartyData
import network.warzone.mars.match.tracker.KillstreakTracker
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.utils.matchPlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.api.party.Competitor
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.ParticipantState
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

    @Command(aliases = ["ping"], desc = "View a player's ping", usage = "[player]")
    fun onPingView(@Sender sender: CommandSender, @Nullable @PlayerName playerName: String?) {
        val player = playerName?.let { Bukkit.getPlayer(it) } ?: sender
        if (player !is Player) throw CommandException("Consoles cannot check their own pings")

        val possessive = "${player.name}${ChatColor.GRAY}${if (player.name.endsWith("s")) "'" else "'s"}"
        val ping = player.spigot().ping
        if (player == sender) sender.sendMessage("${ChatColor.GRAY}Your ping is ${ChatColor.AQUA}${ping}ms")
        else sender.sendMessage("${ChatColor.AQUA}$possessive ${ChatColor.GRAY}ping is ${ChatColor.AQUA}${ping}ms")
    }

    @Command(
        aliases = ["tp"],
        desc = "Teleport to a player",
        usage = "<player> [otherPlayer]",
        perms = ["mars.tp", "mars.tp.other"])
    fun onTeleportToPlayer(
        @Sender sender: Player,
        @Nullable @PlayerName playerOneName: String?,
        @Nullable @PlayerName playerTwoName: String?)
    {
        val targetPlayerOne = playerOneName?.let { Bukkit.getPlayer(it) }
        val targetPlayerTwo = playerTwoName?.let { Bukkit.getPlayer(it) }

        // Prevent even mods from using command if they are participating
        // in the match. Opped players do not apply.
        if (sender.matchPlayer.isParticipating && !sender.isOp) {
            sender.sendMessage("You cannot use this command while participating!")
            return
        }
        // Scenario One: Both arguments are empty.
        if (targetPlayerOne == null) {
            sender.sendMessage("You must provide a player to teleport to!")
            return
        }
        // Scenario Two: Only argument two is empty.
        if (targetPlayerTwo == null) {
            sender.teleport(targetPlayerOne.location)
            sender.sendMessage("Teleported to " + playerOneName)
            return
        }
        // Scenario Three: Both arguments are filled.
        if (sender.hasPermission("mars.tp.other")) {
            targetPlayerOne.teleport(targetPlayerTwo.location)
            sender.sendMessage("Teleported " + playerOneName + " to " + playerTwoName)
            return
        }

        sender.sendMessage("If you see this message, something has gone wrong.")
    }
}
