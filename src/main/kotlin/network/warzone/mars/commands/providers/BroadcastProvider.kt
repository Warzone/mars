package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import kotlinx.coroutines.runBlocking
import network.warzone.mars.broadcast.Broadcast
import network.warzone.mars.broadcast.BroadcastFeature
import network.warzone.mars.broadcast.exceptions.BroadcastMissingException
import network.warzone.mars.utils.translate
import org.bukkit.command.CommandSender

class BroadcastProvider : BukkitProvider<Broadcast> {
    override fun getName(): String {
        return "broadcast"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Broadcast = runBlocking {
        val broadcastName = args.next()

        val broadcast: Broadcast? = BroadcastFeature.fetch(broadcastName)
        broadcast ?: throw ArgumentParseException(
            translate(BroadcastMissingException(broadcastName).asComponent(), sender)
        )

        return@runBlocking broadcast
    }

    override fun getSuggestions(
        prefix: String,
        sender: CommandSender,
        namespace: Namespace,
        mods: MutableList<out Annotation>
    ): List<String> {
        val query = prefix.toLowerCase()

        return BroadcastFeature.getBroadcasts()
            .map { it.name.toLowerCase() }
            .filter { it.startsWith(query) }
    }
}