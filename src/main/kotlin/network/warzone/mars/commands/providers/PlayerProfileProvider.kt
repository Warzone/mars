package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import com.github.kittinunf.result.getOrNull
import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class PlayerProfileProvider : BukkitProvider<PlayerProfile> {
    override fun getName(): String {
        return "player"
    }

    override fun get(
        sender: CommandSender?,
        args: CommandArgs,
        annotations: MutableList<out Annotation>?
    ): PlayerProfile = runBlocking {
        val name = args.next()

        val profile = PlayerFeature.get(name)
        profile ?: throw ArgumentParseException(PlayerMissingException(name).asTextComponent().content())

        return@runBlocking profile
    }

    override fun getSuggestions(
        prefix: String?,
        namespace: Namespace?,
        modifiers: MutableList<out Annotation>?
    ): MutableList<String> {
        return Bukkit.getOnlinePlayers()
            .filter { it.name.startsWith(prefix ?: "") }
            .map { it.name }
            .toMutableList()
    }
}