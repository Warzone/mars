package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.rank.exceptions.RankMissingException
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.translate
import org.bukkit.command.CommandSender

class RankProvider : BukkitProvider<Rank> {
    override fun getName(): String {
        return "rank"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Rank {
        val rankName = args.next()

        val rank: Rank? = RankFeature.getCached(rankName)
        rank ?: throw ArgumentParseException(
            translate(RankMissingException(rankName).asComponent(), sender)
        )

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