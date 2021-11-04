package network.warzone.mars.tags.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tags.TagFeature
import network.warzone.mars.tags.models.Tag
import network.warzone.mars.utils.asTextComponent
import org.bukkit.ChatColor.GREEN
import org.bukkit.command.CommandSender
import javax.annotation.Nullable

class TagCommand {

    @Command( aliases = ["create", "new"], desc = "Creates a new tag")
    fun onTagCreate(sender: CommandSender, audience: Audience, name: String, display: String) = runBlocking {
        TagFeature.createTag(name, display)
            .fold(
                { sender.sendMessage("${GREEN}Created tag $name") },
                { audience.sendMessage(it.asTextComponent()) }
            )
    }

    @Command( aliases = ["delete", "rm"], desc = "Deletes a rank." )
    fun onTagDelete(sender: CommandSender, audience: Audience, tag: Tag) = runBlocking {
        TagFeature.deleteTag(tag._id)
            .fold(
                { sender.sendMessage("${GREEN}Deleted tag ${tag.name}") },
                { audience.sendMessage(it.asTextComponent()) }
            )
    }

    @Command( aliases = ["update", "edit", "modify"], desc = "Updates a rank.")
    fun onTagUpdate(sender: CommandSender, audience: Audience, tag: Tag, targetProperty: String, value: String) = runBlocking {
        // Create temporary copy for unchecked modifications.
        val mutableTag = tag.copy()

        when (targetProperty) {
            "name" -> mutableTag.name = value
            "display" -> mutableTag.display = value
            else -> throw CommandException("Invalid property: $targetProperty")
        }

        //val difference = tag.diff(mutableTag, Tag::class)

        TagFeature
            .updateTag(tag._id, mutableTag)
            .fold(
                {
                    sender.sendMessage("${GREEN}Tag updated.")
                    //difference.map { it.asTextComponent() }.forEach(audience::sendMessage)
                },
                { audience.sendMessage(it.asTextComponent()) }
            )
    }

    @Command(aliases = ["list"], desc = "Lists all the rank.")
    fun onTagList(sender: CommandSender, audience: Audience) = runBlocking {
        val tags = TagFeature.list()

        sender.sendMessage("${GREEN}Tags:")
        tags
            .map { it.asTextComponent(true) }
            .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
            .forEach { audience.sendMessage(it) }
    }

    @Command( aliases = ["player"], desc = "Manages a player's tags")
    fun onTagPlayer(sender: CommandSender, audience: Audience, targetPlayer: String, operation: String, @Nullable tag: Tag?) = runBlocking {
        val player = PlayerManager.getPlayer(targetPlayer) ?: throw CommandException("Invalid player.") //TODO: support offline players.

        when (operation) {
            "add" -> {
                tag ?: throw CommandException("No tag provided.")

                PlayerFeature.addTag(player, tag).fold(
                    { sender.sendMessage("${GREEN}Added ${tag.name} to user.") },
                    { audience.sendMessage(it.asTextComponent()) }
                )
            }
            "remove" -> {
                tag ?: throw CommandException("No tag provided.")

                PlayerFeature.removeTag(player, tag).fold(
                    { sender.sendMessage("${GREEN}Removed ${tag.name} to user.") },
                    { audience.sendMessage(it.asTextComponent()) }
                )
            }
            "list" -> {
                val tags = player.getPlayerProfile().tags()

                sender.sendMessage("${GREEN}Tags:")
                tags
                    .map { it.asTextComponent(true) }
                    .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
                    .forEach { audience.sendMessage(it) }
            }
            else -> throw CommandException("Invalid operation $operation.")
        }
    }

}