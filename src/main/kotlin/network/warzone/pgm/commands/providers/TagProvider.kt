package network.warzone.pgm.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import app.ashcon.intake.parametric.ProvisionException
import kotlinx.coroutines.runBlocking
import network.warzone.pgm.tags.TagFeature
import network.warzone.pgm.tags.models.Tag
import org.bukkit.command.CommandSender

class TagProvider : BukkitProvider<Tag> {

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Tag = runBlocking {
        val tagName = args.next()

        TagFeature
            .get(tagName)
            .fold(
                { it },
                { throw ProvisionException(it.message) }
            )
    }

    override fun isProvided(): Boolean {
        return true
    }

}