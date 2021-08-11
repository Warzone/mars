package network.warzone.pgm.ranks.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.parametric.annotation.Default
import app.ashcon.intake.parametric.annotation.Switch
import app.ashcon.intake.parametric.annotation.Text
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import network.warzone.pgm.player.PlayerManager
import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.asTextComponent
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import javax.annotation.Nullable

class RankCommands {

    @Command(aliases = ["create", "new"], desc = "Creates a rank")
    fun onRankCreate(
        sender: CommandSender,
        audience: Audience,
        name: String,
        @Default("0") priority: Int?,
        @Nullable displayName: String?,
        @Nullable prefix: String?,
        @Switch('s') isStaff: Boolean,
        @Switch('d') isDefault: Boolean
    ) = runBlocking {
        RankFeature.createRank(
            name = name,
            priority = priority,
            displayName = displayName,
            prefix = prefix,
            staff = isStaff,
            applyOnJoin = isDefault
        ).fold(
            { sender.sendMessage("${GREEN}Created rank $YELLOW$name") },
            { audience.sendMessage(it.asTextComponent()) }
        )
    }

    @Command(aliases = ["delete", "rm"], desc = "Deletes a rank")
    fun onRankDelete(sender: CommandSender, audience: Audience, targetRank: Rank) = runBlocking {
        RankFeature.deleteRank(targetRank._id).fold(
            { sender.sendMessage("${GREEN}Deleted rank $YELLOW${targetRank.name}") },
            { audience.sendMessage(it.asTextComponent()) }
        )
    }

    @Command(aliases = ["list", "ls"], desc = "Lists the ranks")
    fun onRankList(sender: CommandSender, audience: Audience) = runBlocking {
        val ranks = RankFeature.list()

        if (ranks.isEmpty()) {
            sender.sendMessage("${RED}No ranks :(")
            return@runBlocking
        }

        sender.sendMessage("${GREEN}Ranks:")
        ranks
            .map(Rank::asTextComponent)
            .forEach(audience::sendMessage)
    }

    @Command(aliases = ["update", "edit", "modify"], desc = "Edits a rank")
    fun onRankUpdate(
        sender: CommandSender,
        audience: Audience,
        rank: Rank,
        targetProperty: String,
        @Text value: String
    ) = runBlocking {
        val mutableRank = rank.copy()

        if (targetProperty == "permissions") {
            val values = value.split(' ').toMutableList()

            val before = mutableRank.permissions.count()

            when (values.removeFirst()) {
                "add" -> mutableRank.permissions.addAll(values)
                "remove" -> mutableRank.permissions.removeAll(values)
                "clear" -> mutableRank.permissions.clear()
                else -> throw CommandException("Invalid action for permissions.")
            }

            val after = mutableRank.permissions.count()
            val change = after - before
            val changeDisplay = if (change > 0) "$GREEN+$change" else "$RED-$change"
            val cleared = before == -change

            RankFeature.updatePermissions(rank)
            RankFeature.updateRank(rank._id, mutableRank).fold(
                {
                    if (cleared) sender.sendMessage("${GREEN}Cleared permissions.")
                    else sender.sendMessage("${GREEN}Updated permissions. $WHITE$BOLD$before $RESET$GRAY-> $WHITE$BOLD$after $GRAY($changeDisplay$GRAY)")
                },
                {
                    audience.sendMessage(it.asTextComponent())
                }
            )
        } else {
            when (targetProperty) {
                "name" -> mutableRank.name = value
                "display" -> mutableRank.displayName = value
                "priority" -> mutableRank.priority = value.toInt()
                "prefix" -> mutableRank.prefix = if (value == "clear") null else value
                "staff" -> mutableRank.staff = value.toBoolean()
                "default" -> mutableRank.applyOnJoin = value.toBoolean()
                else -> throw CommandException("Invalid property.")
            }

            RankFeature
                .updateRank(rank._id, mutableRank)
                .fold(
                    { sender.sendMessage("${GREEN}Updated permissions.") },
                    { audience.sendMessage(it.asTextComponent()) }
                )
        }
    }

    @Command( aliases = ["player", "p"], desc = "Operations on a players ranks." )
    fun onRankPlayer(sender: CommandSender, audience: Audience, playerName: String, operation: String, @Nullable rank: Rank?) = runBlocking {
        val player = PlayerManager.getPlayer(playerName) ?: throw CommandException("Invalid player.")

        when (operation) {
            "add" -> {
                rank ?: throw CommandException("No rank provided.")

                PlayerFeature.addRank(player, rank).fold(
                    { sender.sendMessage("${GREEN}Added ${rank.name} to user.") },
                    { audience.sendMessage(it.asTextComponent()) }
                )
            }
            "remove" -> {
                rank ?: throw CommandException("No rank provided.")

                PlayerFeature.removeRank(player, rank).fold(
                    { sender.sendMessage("${GREEN}Removed ${rank.name} to user.") },
                    { audience.sendMessage(it.asTextComponent()) }
                )
            }
            "list" -> {
                val ranks = player.getPlayerProfile().ranks

                sender.sendMessage("${GREEN}Ranks:")
                ranks
                    .map { it.get() }
                    .map { it.asTextComponent() }
                    .forEach { audience.sendMessage(it) }
            }
            else -> throw CommandException("Invalid property.")
        }
    }

}