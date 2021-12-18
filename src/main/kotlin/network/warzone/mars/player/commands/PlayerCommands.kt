package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.chat.BaseComponent
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.utils.*
import org.bukkit.command.CommandSender
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.*
import org.bukkit.ChatColor
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.time.Duration

class PlayerCommands {

    @Command(aliases = ["lookup", "alts", "lu"], desc = "Lookup player information & alts")
    fun onPlayerLookup(@Sender sender: CommandSender, audience: Audience, context: PlayerContext, target: String) =
        runBlocking {
            val lookup = PlayerService.lookupPlayer(target).get() ?: throw CommandException("Invalid player")
            val player = lookup.player

            val isOnline = PlayerManager.getPlayer(player._id) != null

            var message = text()
                .append { createStandardLabelled("Name", player.name) }
                .append { createUncolouredLabelled("ID", player._id.toString()) }
                .append {
                    createUncolouredLabelled(
                        "First Joined",
                        "${player.firstJoinedAt} (${player.firstJoinedAt.getTimeAgo()})"
                    )
                }
                .append {
                    createUncolouredLabelled(
                        "Last Joined",
                        if (isOnline) "${ChatColor.GREEN}Online" else "${player.lastJoinedAt} (${player.lastJoinedAt.getTimeAgo()}"
                    )
                }
                .append { createUncolouredLabelled("Playtime", Duration.ofMillis(player.stats.serverPlaytime).conciseFormat()) }
                .append { createUncolouredLabelled("Alts", "") }

            lookup.alts.forEach {
                message = message
                    .append { text("-", NamedTextColor.GRAY) }
                    .append { space() }
                    .append { it.asTextComponent() }
                    .append { newline() }
            }

            context.matchPlayer.sendMessage(message)
        }
}

