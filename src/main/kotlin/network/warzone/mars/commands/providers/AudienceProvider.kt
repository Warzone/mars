package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import tc.oc.pgm.lib.net.kyori.adventure.audience.Audience
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import org.bukkit.command.CommandSender

class AudienceProvider : BukkitProvider<Audience> {
    override fun get(sender: CommandSender, args: CommandArgs?, list: MutableList<out Annotation>?): Audience {
        return AUDIENCE_PROVIDER.sender(sender)
    }

    override fun isProvided(): Boolean {
        return true
    }
}