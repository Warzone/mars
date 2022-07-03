package network.warzone.mars.tag.commands

import app.ashcon.intake.Command
import app.ashcon.intake.CommandException
import app.ashcon.intake.parametric.annotation.Switch
import network.warzone.mars.Mars
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.tag.TagFeature
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.asTextComponent
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.ITALIC
import org.bukkit.command.CommandSender
import javax.annotation.Nullable

class TagCommand {
    @Command(
        aliases = ["create", "new"],
        desc = "Create a new tag",
        usage = "<name> <display>",
        perms = ["mars.tags.manage"]
    )
    fun onTagCreate(sender: CommandSender, audience: Audience, name: String, display: String) {
        Mars.async {
            try {
                TagFeature.create(name, display)
                sender.sendMessage("${GREEN}Created tag $name")
            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }
    }

    @Command(aliases = ["delete", "rm"], desc = "Delete a tag", usage = "<tag>", perms = ["mars.tags.manage"])
    fun onTagDelete(sender: CommandSender, audience: Audience, tag: Tag) {
        Mars.async {
            try {
                TagFeature.delete(tag._id)
                sender.sendMessage("${GREEN}Deleted tag ${tag.name}")
            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }
    }

    @Command(
        aliases = ["update", "edit", "modify"],
        desc = "Update a tag",
        usage = "<tag> <'name'|'display'> <new value>",
        perms = ["mars.tags.manage"]
    )
    fun onTagUpdate(sender: CommandSender, audience: Audience, tag: Tag, targetProperty: String, value: String) {
        Mars.async {
            // Create temporary copy for unchecked modifications.
            val mutableTag = tag.copy()

            when (targetProperty) {
                "name" -> mutableTag.name = value
                "display" -> mutableTag.display = value
                else -> throw CommandException("Invalid property: $targetProperty")
            }

            try {
                TagFeature.update(tag._id, mutableTag)
                sender.sendMessage("${GREEN}Tag updated")
            } catch (e: FeatureException) {
                audience.sendMessage(e.asTextComponent())
            }
        }
    }

    @Command(aliases = ["list"], desc = "List all tags", perms = ["mars.tags.manage"])
    fun onTagList(sender: CommandSender, audience: Audience) {
        Mars.async {
            val tags = TagFeature.list()

            sender.sendMessage("${GREEN}Tags:")
            tags
                .map { it.asTextComponent(true) }
                .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
                .forEach { audience.sendMessage(it) }
        }
    }

    @Command(
        aliases = ["player"],
        desc = "Manage a player's tags",
        usage = "<player> <'add'|'remove'|'set'|'list'> [tag]",
        perms = ["mars.tags.manage"]
    )
    fun onTagPlayer(
        sender: CommandSender,
        audience: Audience,
        playerName: String,
        operation: String,
        @Nullable tag: Tag?,
        @Switch('f') force: Boolean = false
    ) {
        Mars.async {
            val player = PlayerFeature.get(playerName) ?: throw CommandException("Invalid player")

            when (operation) {
                "add" -> {
                    tag ?: throw CommandException("No tag provided.")

                    try {
                        PlayerFeature.addTag(player.name, tag.name)
                        sender.sendMessage("${GREEN}Added ${tag.name} to player")
                    } catch (e: FeatureException) {
                        audience.sendMessage(e.asTextComponent())
                    }
                }
                "remove" -> {
                    tag ?: throw CommandException("No tag provided.")

                    try {
                        PlayerFeature.removeTag(player.name, tag.name)
                        sender.sendMessage("${GREEN}Removed ${tag.name} from player")
                    } catch (e: FeatureException) {
                        audience.sendMessage(e.asTextComponent())
                    }
                }
                "list" -> {
                    val tags = player.tags()

                    sender.sendMessage("${GREEN}Tags:")
                    tags
                        .map { it.asTextComponent(true) }
                        .map { Component.text("- ", NamedTextColor.GRAY).append(it) }
                        .forEach { audience.sendMessage(it) }
                }
                "set" -> {
                    if (tag != null) {
                        try {
                            val tags = player.tags()
                            if (!tags.contains(tag) && force) PlayerFeature.addTag(player.name, tag.name)
                            PlayerFeature.setActiveTag(player.name, tag)
                            sender.sendMessage("${GREEN}Set player's active tag to ${tag.name}")
                        } catch (e: FeatureException) {
                            audience.sendMessage(e.asTextComponent())
                        }
                    } else {
                        try {
                            val activeTag = player.activeTag()
                            PlayerFeature.setActiveTag(player.name, null)
                            sender.sendMessage("${RED}Cleared player's active tag (previous: ${activeTag?.name ?: "${ITALIC}None"})")
                        } catch (e: FeatureException) {
                            audience.sendMessage(e.asTextComponent())
                        }
                    }
                }
                else -> throw CommandException("Invalid operation $operation.")
            }
        }
    }
}