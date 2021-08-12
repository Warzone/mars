package network.warzone.pgm.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.exceptions.RankMissingException
import network.warzone.pgm.ranks.models.Rank
import org.bukkit.command.CommandSender

class RankProvider : BukkitProvider<Rank> {

    override fun getName(): String {
        return "rank"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Rank {
        val rankName = args.next()

        val rank: Rank? = RankFeature.getCached(rankName)
        rank ?: throw ArgumentParseException(RankMissingException(rankName).asTextComponent().content())

        return rank
    }

    override fun getSuggestions(
        prefix: String,
        sender: CommandSender,
        namespace: Namespace,
        mods: MutableList<out Annotation>
    ): List<String> {
        val query = prefix.toLowerCase()

        return RankFeature.cache.values
            .map { it.nameLower }
            .filter { it.startsWith(query) }
    }

}