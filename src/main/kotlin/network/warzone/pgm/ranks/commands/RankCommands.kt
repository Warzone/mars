package network.warzone.pgm.ranks.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.parametric.annotation.Default
import app.ashcon.intake.parametric.annotation.Switch
import app.ashcon.intake.parametric.annotation.Text
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.asTextComponent
import network.warzone.pgm.utils.command
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender

class RankCommands {

    @Command(aliases = ["create", "new"], desc = "Creates a rank")
    fun onRankCreate(
        sender: CommandSender,
        name: String,
        @Default("0") priority: Int?,
        displayName: String?,
        prefix: String?,
        @Switch('s') isStaff: Boolean,
        @Switch('d') isDefault: Boolean
    ) = runBlocking {
        command {
            RankFeature.createRank(
                name = name,
                priority = priority,
                displayName = displayName,
                prefix = prefix,
                staff = isStaff,
                applyOnJoin = isDefault
            )
        }

        sender.sendMessage("${GREEN}Created rank $YELLOW$name")
    }

    @Command(aliases = ["delete", "rm"], desc = "Deletes a rank")
    fun onRankDelete(sender: CommandSender, targetRank: Rank) = runBlocking {
        command { RankFeature.deleteRank(targetRank._id) }

        sender.sendMessage("${GREEN}Deleted rank $YELLOW$targetRank")
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
        rank: Rank,
        targetProperty: String,
        @Text value: String
    ) = runBlocking {
        if (targetProperty === "permissions") {
            // Handle permission logic
            val values = value.split(' ').toMutableList()

            when (values.removeFirst()) {
                "add" -> rank.permissions.addAll(values)
                "remove" -> rank.permissions.removeAll(values)
                "clear" -> rank.permissions.clear()
                else -> throw CommandException("Invalid action for permissions.")
            }
        } else {
            // Every other property
            when (targetProperty) {
                "name" -> rank.name = value
                "display" -> rank.displayName = value
                "priority" -> rank.priority = value.toInt()
                "prefix" -> if (value == "clear") rank.prefix = null else rank.prefix = value
                "staff" -> rank.staff = value.toBoolean()
                "default" -> rank.applyOnJoin = value.toBoolean()
                else -> throw CommandException("Invalid property.")
            }
        }

        // TODO: if updating fails, invalidate rank since it's modified, and/or save copy and apply earlier version.
        command {  RankFeature.updateRank(rank) }
    }

}