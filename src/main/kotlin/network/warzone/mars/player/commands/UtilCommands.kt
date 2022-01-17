package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.Mars
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UtilCommands {
    @Command(aliases = ["discord"], desc = "Join the Discord server")
    fun discordInvite(@Sender sender: CommandSender) {
        val invite = Mars.get().config.getString("server.links.discord") ?: "No Discord invite available"
        sender.sendMessage("${ChatColor.AQUA}$invite")
    }

    @Command(aliases = ["gmc"], desc = "Set your gamemode to Creative", perms = ["mars.gmc"])
    fun onCreativeSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.CREATIVE
    }

    @Command(aliases = ["gms"], desc = "Set your gamemode to Survival", perms = ["mars.gms"])
    fun onSurvivalSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.SURVIVAL
    }

    @Command(aliases = ["gmsp"], desc = "Set your gamemode to Spectator", perms = ["mars.gmsp"])
    fun onSpectatorSwitch(@Sender sender: Player) {
        sender.gameMode = GameMode.SPECTATOR
    }
}