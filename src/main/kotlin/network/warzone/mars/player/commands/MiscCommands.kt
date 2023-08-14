package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.Mars
import network.warzone.mars.commands.providers.PlayerName
import network.warzone.mars.player.achievements.AchievementFeature.printAchievements
import network.warzone.mars.player.achievements.AchievementManager
import network.warzone.mars.player.achievements.gui.AchievementMenu
import network.warzone.mars.utils.matchPlayer
import network.warzone.mars.utils.menu.open
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
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
        aliases = ["achievements"],
        desc = "Open the achievements menu",
        perms = ["mars.achievements"]
    )
    fun onAchievementMenuRequest(
        @Sender sender: Player,
        audience: Audience,
        @Nullable arg1: String?
    )
    {
        /** TODO: An AchievementMenu instance is currently not persistent for each player.
         *   This means each time /achievements is ran, the menu is created from scratch.
         *   Idk how big of a deal it is that this menu must be rebuilt each time the
         *   command is ran, but perhaps a Map<Player, AchievementMenu> variable could
         *   be used to create persistence if needed.
         */
        if (arg1 == "print") {
            Mars.async {
                printAchievements()
            }
        }
        else if (arg1 == "menu") {
            sender.open(AchievementMenu(sender).openMainMenu())
        }
        else {
            audience.sendMessage(text("Invalid argument \"$arg1\".", NamedTextColor.RED))
        }
    }

    @Command(
        aliases = ["tp"],
        desc = "Teleport to a player",
        perms = ["mars.tp"]
    )
    fun onTeleportToPlayer(
        @Sender sender: Player,
        audience: Audience,
        @Nullable @PlayerName playerOneName: String?,
        @Nullable @PlayerName playerTwoName: String?)
    {
        // mars.tp.other: Allow player to teleport players to other players.
        // mars.tp.bypass: Allow player to teleport while participating in match.

        val targetPlayerOne = playerOneName?.let { Bukkit.getPlayer(it) }
        val targetPlayerTwo = playerTwoName?.let { Bukkit.getPlayer(it) }

        // Do not allow players to use command while in a match.
        if (sender.matchPlayer.isParticipating && !sender.hasPermission("mars.tp.bypass")) {
            audience.sendMessage(text("You cannot use this command while participating!", NamedTextColor.RED))
            return
        }

        // Handle usage feedback when no args are provided
        if (playerOneName == null) {
            if (sender.hasPermission("mars.tp.other"))
                audience.sendMessage(text("Usage: /tp <player> [targetPlayer]", NamedTextColor.RED))
            else
                audience.sendMessage(text("Usage: /tp <player>", NamedTextColor.RED))
            return
        }

        // If an invalid player name is entered, notify sender.
        if (targetPlayerOne == null) {
            audience.sendMessage(text("Could not find the player \"" + playerOneName + "\"", NamedTextColor.RED))
            return
        }

        if (playerTwoName != null) {
            // Tell a player without proper permissions that they cannot
            // tp others if they try to input a second argument in the command.
            if (!sender.hasPermission("mars.tp.other")) {
                audience.sendMessage(text("You cannot teleport players to other players!", NamedTextColor.RED))
            }
            // If an invalid player name is entered, notify sender.
            else if (targetPlayerTwo == null) {
                audience.sendMessage(text("Could not find the player \"" + playerTwoName + "\"", NamedTextColor.RED))
            }
            // Teleport player to other player after verifying conditions.
            else {
                targetPlayerOne.teleport(targetPlayerTwo.location)
                audience.sendMessage(text("Teleported " + playerOneName + " to " + playerTwoName, NamedTextColor.YELLOW))
            }
            return
        }

        // Teleport to a player.
        sender.teleport(targetPlayerOne.location)
        audience.sendMessage(text("Teleported to " + playerOneName, NamedTextColor.YELLOW))
    }
}
