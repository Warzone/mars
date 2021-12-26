package network.warzone.mars.commands.providers

import app.ashcon.intake.CommandException
import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import com.github.kittinunf.result.getOrNull
import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import org.bukkit.command.CommandSender

class PlayerProfileProvider : BukkitProvider<PlayerProfile> {

    override fun getName(): String {
        return "player"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): PlayerProfile = runBlocking {
        val name = args.next()

        val profile = PlayerFeature.get(name).getOrNull()
        println("profile ${profile?.name}")
        profile ?: throw ArgumentParseException(PlayerMissingException(name).asTextComponent().content())

        return@runBlocking profile
    }

}