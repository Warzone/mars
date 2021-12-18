package network.warzone.mars.rank.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.parametric.annotation.Default
import app.ashcon.intake.parametric.annotation.Switch
import app.ashcon.intake.parametric.annotation.Text
import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.asTextComponent
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
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
            .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
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
            val afterWithDuplicates = mutableRank.permissions.count()
            mutableRank.permissions = mutableRank.permissions.distinct().toMutableList()

            val after = mutableRank.permissions.count()
            val change = after - before
            val changeDisplay = if (change > 0) "$GREEN+$change" else if (change == 0) "${YELLOW}0" else "$RED$change"
            val cleared = after == 0
            val duplicates = afterWithDuplicates - after

            RankFeature.updatePermissions(rank)
            RankFeature.updateRank(rank._id, mutableRank).fold(
                {
                    if (cleared) sender.sendMessage("${GREEN}Cleared permissions.")
                    else sender
                            .sendMessage("${GREEN}Updated permissions." +
                                    "\n$WHITE$BOLD$before $RESET$GRAY-> $WHITE$BOLD$after $GRAY($changeDisplay$GRAY) " +
                                    "[$duplicates duplicate${if (duplicates == 1) "" else "s"} ignored]")
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
                else -> throw CommandException("Invalid property $targetProperty.")
            }

            //val difference = rank.diff(mutableRank, Rank::class)

            RankFeature
                .updateRank(rank._id, mutableRank)
                .fold(
                    {
                        sender.sendMessage("${GREEN}Updated rank.")
                        //difference.map { it.asTextComponent() }.forEach(audience::sendMessage)
                    },
                    { audience.sendMessage(it.asTextComponent()) }
                )
        }
    }

    @Command( aliases = ["player", "p"], desc = "Operations on a players ranks." )
    fun onRankPlayer(sender: CommandSender, audience: Audience, playerName: String, operation: String, @Nullable rank: Rank?) = runBlocking {
        val player = PlayerManager.getPlayer(playerName) ?: throw CommandException("Invalid player.") //TODO: support offline players.

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
                val ranks = player.getPlayerProfile().ranks()

                sender.sendMessage("${GREEN}Ranks:")
                ranks
                    .map { it.asTextComponent() }
                    .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
                    .forEach { audience.sendMessage(it) }
            }
            else -> throw CommandException("Invalid operation $operation.")
        }
    }

}