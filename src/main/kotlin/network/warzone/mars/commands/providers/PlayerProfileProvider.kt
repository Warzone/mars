package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.feature.exceptions.PlayerNotOnlineException
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
    ): PlayerProfile {
        val name = args.next()

        val profile = PlayerFeature.getCached(name) // Only online players
        profile ?: throw ArgumentParseException(PlayerNotOnlineException(name).asTextComponent().content())

        return profile
    }

    override fun getSuggestions(
        prefix: String?,
        namespace: Namespace?,
        modifiers: MutableList<out Annotation>?
    ): MutableList<String> {
        return Bukkit.getOnlinePlayers()
            .filter { it.name.startsWith(prefix ?: "", ignoreCase = true) }
            .map { it.name }
            .toMutableList()
    }
}