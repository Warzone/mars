package network.warzone.pgm.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import app.ashcon.intake.parametric.ProvisionException
import kotlinx.coroutines.runBlocking
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.models.Rank
import org.bukkit.command.CommandSender

class RankProvider : BukkitProvider<Rank> {

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Rank = runBlocking {
        val rankName = args.next()

        RankFeature
            .get(rankName)
            .fold(
                { it },
                { throw ProvisionException(it.message) }
            )
    }

    override fun isProvided(): Boolean {
        return true
    }

}