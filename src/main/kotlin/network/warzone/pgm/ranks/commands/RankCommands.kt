package network.warzone.pgm.ranks.commands

import app.ashcon.intake.Command
import app.ashcon.intake.parametric.annotation.Default
import app.ashcon.intake.parametric.annotation.Switch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import network.warzone.pgm.ranks.RankFeature
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.asTextComponent
import network.warzone.pgm.utils.verify
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender

class RankCommands {

    @Command( aliases = ["create", "new"], desc = "Creates a rank")
    fun onRankCreate(sender: CommandSender,
                     name: String,
                     @Default("0") priority: Int?,
                     displayName: String?,
                     prefix: String?,
                     @Switch('s') isStaff: Boolean,
                     @Switch('d') isDefault: Boolean
    ) = runBlocking {
        verify {
            RankFeature.createRank(
                name = name,
                priority = priority,
                displayName = displayName,
                prefix = prefix,
                staff = isStaff,
                applyOnJoin = isDefault
            )
        }

        sender.sendMessage("${GREEN}Created rank $YELLOW$name")
    }

    @Command(aliases = ["delete", "rm"], desc = "Deletes a rank")
    fun onRankDelete(sender: CommandSender, targetRank: String) = runBlocking {
        verify { RankFeature.deleteRank(targetRank) }

        sender.sendMessage("${GREEN}Deleted rank $YELLOW$targetRank")
    }

    @Command(aliases = ["list", "ls"], desc = "Lists the ranks")
    fun onRankList(sender: CommandSender, audience: Audience) = runBlocking {
        val ranks = RankFeature.list()

        if (ranks.isEmpty()) {
            sender.sendMessage("${RED}No ranks :(")
            return@runBlocking
        }

        sender.sendMessage("${GREEN}Ranks:")
        ranks
            .map(Rank::asTextComponent)
            .forEach(audience::sendMessage)
    }

}