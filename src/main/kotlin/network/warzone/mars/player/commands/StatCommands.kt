package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.match.tracker.KillstreakTracker
import network.warzone.mars.utils.matchPlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import javax.annotation.Nullable

class StatCommands {
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
}