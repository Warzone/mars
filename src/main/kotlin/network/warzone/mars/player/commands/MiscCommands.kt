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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import javax.annotation.Nullable

class MiscCommands {
    @Command(aliases = ["appeal"], desc = "Get a direct link to the appeal site")
    fun onAppealLink(@Sender sender: Player) {
        val appealLink = Mars.get().config.getString("server.links.appeal") ?: "No appeal link available"
        sender.matchPlayer.sendMessage(
            Component.text("Click here to appeal a punishment", NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(appealLink))
        )
    }

    @Command(aliases = ["rules"], desc = "Get a direct link to the rules site")
    fun onRulesLink(@Sender sender: CommandSender) {
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
        perms = ["mars.tp"]
        // mars.tp.other: Allow player to teleport players to other players.
        // mars.tp.bypass: Allow player to teleport while participating in match.
        )
    fun onTeleportToPlayer(
        @Sender sender: Player,
        @Nullable @PlayerName playerOneName: String?,
        @Nullable @PlayerName playerTwoName: String?)
    {
        val targetPlayerOne = playerOneName?.let { Bukkit.getPlayer(it) }
        val targetPlayerTwo = playerTwoName?.let { Bukkit.getPlayer(it) }

        // Do not allow players to use command while in a match.
        if (sender.matchPlayer.isParticipating && !sender.hasPermission("mars.tp.bypass")) {
            sender.sendMessage("${ChatColor.RED} You cannot use this command while participating!");
            return;
        }

        // Handle usage feedback when no args are provided
        if (playerOneName == null && !sender.hasPermission("mars.tp.other")) {
            sender.sendMessage("${ChatColor.RED} Usage: /tp <player>")
            return;
        }
        if (playerOneName == null && sender.hasPermission("mars.tp.other")) {
            sender.sendMessage("${ChatColor.RED} Usage: /tp <player> [targetPlayer]");
            return;
        }

        // Tell a player without proper permissions that they cannot
        // tp others if they try to input a second argument in the command.
        if (playerTwoName != null && !sender.hasPermission("mars.tp.other")) {
            sender.sendMessage("${ChatColor.RED} You cannot teleport players to other players!");
            return;
        }

        // If an invalid player name is entered, notify sender.
        if (targetPlayerOne == null) {
            sender.sendMessage("${ChatColor.RED} Could not find the player \"" + playerOneName + "\"");
            return;
        }
        if (playerTwoName != null && targetPlayerTwo == null) {
            sender.sendMessage("${ChatColor.RED} Could not find the player \"" + playerTwoName + "\"");
            return;
        }

        // Teleport player to other player after verifying conditions.
        if (targetPlayerTwo != null) {
            targetPlayerOne.teleport(targetPlayerTwo.location)
            sender.sendMessage("${ChatColor.YELLOW} Teleported " + playerOneName + " to " + playerTwoName)
            return;
        }

        // Teleport to a player.
        sender.teleport(targetPlayerOne.location)
        sender.sendMessage("${ChatColor.YELLOW} Teleported to " + playerOneName)
        return;
    }
}
