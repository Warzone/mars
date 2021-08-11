package network.warzone.pgm.tags.commands

import app.ashcon.intake.Command
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import network.warzone.pgm.tags.TagFeature
import network.warzone.pgm.tags.models.Tag
import org.bukkit.ChatColor.GREEN
import org.bukkit.command.CommandSender

class TagCommands {

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

}