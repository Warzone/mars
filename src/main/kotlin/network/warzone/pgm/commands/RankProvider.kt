package network.warzone.pgm.commands

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import kotlinx.coroutines.runBlocking
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.provide
import org.bukkit.command.CommandSender

class RankProvider : BukkitProvider<Rank> {

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Rank = runBlocking {
        val rankName = args.next()

        return@runBlocking provide { RankFeature.get(rankName) }
    }

}