package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import app.ashcon.intake.parametric.annotation.Text
import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.isFailure
import com.github.kittinunf.result.runCatching
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.chat.BaseComponent
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.*
import org.bukkit.command.CommandSender
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.*
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.time.Duration
import javax.annotation.Nullable

class ModCommands {
    @Command(aliases = ["lookup", "alts", "lu"], desc = "Lookup player information & alts", usage = "<player>", perms = ["mars.lookup"])
    fun onPlayerLookup(@Sender sender: CommandSender, audience: Audience, context: PlayerContext, target: String) =
        runBlocking {
            try {
                val lookup =
                    PlayerFeature.lookup(target, includeAlts = true)
                val player = lookup.player

                val isOnline = PlayerManager.getPlayer(player._id) != null

                var message = text()
                    .append { createStandardLabelled("Name", player.name) }
                    .append { createUncoloredLabelled("ID", player._id.toString()) }
                    .append {
                        createUncoloredLabelled(
                            "First Joined",
                            "${player.firstJoinedAt} (${player.firstJoinedAt.getTimeAgo()})"
                        )
                    }
                    .append {
                        createUncoloredLabelled(
                            "Last Joined",
                            if (isOnline) "${ChatColor.GREEN}Online" else "${player.lastJoinedAt} (${player.lastJoinedAt.getTimeAgo()}"
                        )
                    }
                    .append {
                        createUncoloredLabelled(
                            "Playtime",
                            Duration.ofMillis(player.stats.serverPlaytime).conciseFormat()
                        )
                    }
                    .append { createUncoloredLabelled("Alts", "") }

                lookup.alts.forEach {
                    message = message
                        .append { text("-", NamedTextColor.GRAY) }
                        .append { space() }
                        .append { it.asTextComponent() }
                        .append { newline() }
                }

                context.matchPlayer.sendMessage(message)
            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }


    @Command(
        aliases = ["notes", "note"],
        desc = "Manage a player's staff notes",
        usage = "<player> ['add'|'del'] [content|id]",
        perms = ["mars.notes"]
    )
    fun onNotes(
        @Sender sender: Player,
        audience: Audience,
        context: PlayerContext,
        profile: PlayerProfile,
        @Nullable op: String?,
        @Nullable @Text value: String?
    ) = runBlocking {
        when (op) {
            null -> {
                var message = text("Notes for ${profile.name}", NamedTextColor.GREEN).append { newline() }
                profile.notes.forEach {
                    message = message.append { it.asTextComponent(profile.name, it.author.id == sender.uniqueId) }
                        .append { newline() }
                }
                context.matchPlayer.sendMessage(message)
            }
            "add" -> {
                if (value == null || value.trim().isEmpty()) throw CommandException("No note content provided")
                PlayerFeature.addNote(profile.name, value.trim(), SimplePlayer(sender.uniqueId, sender.name))
                sender.sendMessage("${ChatColor.GREEN}Added note")
            }
            "del" -> {
                if (value == null) throw CommandException("No note ID provided")
                try {
                    val noteId = value.toInt()
                    val note = profile.notes.find { it.id == noteId } ?: throw CommandException("Invalid note ID")
                    if (note.author.id != sender.uniqueId) throw CommandException("You cannot delete this note")

                    PlayerFeature.removeNote(profile.name, noteId)
                    sender.sendMessage("${ChatColor.GREEN}Deleted note #$noteId")
                } catch (ex: Exception) {
                    throw CommandException("Invalid note ID")
                }
            }
            else -> sender.sendMessage("${ChatColor.RED}Available operations: add <content>, del <id>")
        }
    }
}