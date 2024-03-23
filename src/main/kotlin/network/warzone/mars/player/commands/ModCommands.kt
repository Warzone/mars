package network.warzone.mars.player.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import app.ashcon.intake.parametric.annotation.Text
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.commands.providers.PlayerName
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.*
import network.warzone.mars.utils.strategy.multiLine
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import java.time.Duration
import javax.annotation.Nullable

class ModCommands {

    companion object {
        val offlineNameProvider: (String) -> Component = { text(it, NamedTextColor.GRAY) }
    }

    @Command(aliases = ["lookup", "alts", "lu"], desc = "Lookup player information & alts", usage = "<player>", perms = ["mars.lookup"])
    fun onPlayerLookup(@Sender sender: CommandSender, @PlayerName target: String) {
        val match = PGM.get().matchManager.getMatch(sender)!!
        val audience: tc.oc.pgm.util.Audience = tc.oc.pgm.util.Audience.get(sender) // PGM Audience required for name formatting
        Mars.async {
            try {
                val lookup =
                    PlayerFeature.lookup(target, includeAlts = true)
                val player = lookup.player

                val isOnline = PlayerManager.getPlayer(player._id) != null
                var message = audience.multiLine()
                    .appendMultiLineComponent(empty())
                    .appendMultiLine { createStandardLabelledSingleLine("Name", player.name) }
                    .appendMultiLine { createUncoloredLabelledSingleLine("ID", player._id.toString()) }
                    .appendMultiLine {
                        createUncoloredLabelledSingleLine(
                            "First Joined",
                            "${player.firstJoinedAt} (${player.firstJoinedAt.getRelativeTime()})"
                        )
                    }
                    .appendMultiLine {
                        createUncoloredLabelledSingleLine(
                            "Last Joined",
                            if (isOnline) "${ChatColor.GREEN}Online" else "${player.lastJoinedAt} (${player.lastJoinedAt.getRelativeTime()})"
                        )
                    }
                    .appendMultiLine {
                        createUncoloredLabelledSingleLine(
                            "Playtime",
                            Duration.ofMillis(player.stats.serverPlaytime).conciseFormat()
                        )
                    }
                    .appendMultiLine { createUncoloredLabelledSingleLine("Alts", if (lookup.alts.isEmpty()) "(None)" else "") }

                lookup.alts.forEach { alt ->
                    var username = getUsername(alt.player._id, alt.player.name, match, offlineNameProvider = offlineNameProvider)
                    message = message.appendMultiLineComponent(
                        text("-", NamedTextColor.GRAY)
                            .append(space())
                            .append(
                                text("[L]", NamedTextColor.YELLOW)
                                    .clickEvent(ClickEvent.runCommand("/lookup ${alt.player.name}"))
                                    .hoverEvent(text("Click to /lookup ${alt.player.name}", NamedTextColor.YELLOW))
                            )
                            .append(space())
                            .append(
                                text("[P]", NamedTextColor.RED)
                                    .clickEvent(ClickEvent.runCommand("/puns ${alt.player.name}"))
                                    .hoverEvent(text("Click to /puns ${alt.player.name}", NamedTextColor.YELLOW))
                            )
                            .append(space())
                            .append(username)
                    ).appendMultiLineComponent(empty())
                }

                message.deliver()

            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }
    }


    @Command(
        aliases = ["notes", "note"],
        desc = "Manage a player's staff notes",
        usage = "<player> ['add'|'del'] [content|id]",
        perms = ["mars.notes"]
    )
    fun onNotes(
        @Sender sender: CommandSender,
        audience: Audience,
        @PlayerName name: String, // Accept offline players too
        @Nullable op: String?,
        @Nullable @Text value: String?
    )  {
        Mars.async {
            val profile = PlayerFeature.lookup(name).player
            when (op) {
                null -> {
                    var message = audience
                        .multiLine()
                        .appendMultiLineComponent(
                            text("Notes for ${profile.name}", NamedTextColor.GREEN)
                        )

                    profile.notes.forEach {
                        message = message.appendMultiLine {
                            it.asTextComponent(
                                profile.name,
                                sender !is Player || it.author.id == sender.uniqueId
                            )
                        }
                    }

                    message.deliver()
                }
                "add" -> {
                    if (sender !is Player) throw CommandException("${ChatColor.RED}You must be a player to add notes.")
                    if (value == null || value.trim().isEmpty()) throw CommandException("No note content provided")
                    PlayerFeature.addNote(profile.name, value.trim(), SimplePlayer(sender.uniqueId, sender.name))
                    sender.sendMessage("${ChatColor.GREEN}Added note")
                }
                "del" -> {
                    if (value == null) throw CommandException("No note ID provided")
                    try {
                        val noteId = value.toInt()
                        val note = profile.notes.find { it.id == noteId } ?: throw CommandException("Invalid note ID")
                        if (sender is Player && note.author.id != sender.uniqueId) throw CommandException("You cannot delete this note")

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
}
