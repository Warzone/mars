package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import network.warzone.mars.broadcast.Broadcast
import network.warzone.mars.broadcast.BroadcastFeature
import network.warzone.mars.broadcast.exceptions.BroadcastMissingException
import org.bukkit.command.CommandSender

class BroadcastProvider : BukkitProvider<Broadcast> {
    override fun getName(): String {
        return "broadcast"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Broadcast {
        val broadcastName = args.next()

        val broadcast: Broadcast? = BroadcastFeature.fetchCached(broadcastName)
        broadcast ?: throw ArgumentParseException(BroadcastMissingException(broadcastName).asTextComponent().content())

        return broadcast
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