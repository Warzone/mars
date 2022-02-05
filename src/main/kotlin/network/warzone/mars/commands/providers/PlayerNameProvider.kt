package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class PlayerNameProvider : BukkitProvider<String> {

    override fun get(sender: CommandSender, args: CommandArgs, annotations: MutableList<out Annotation>): String? {
        return args.next()
    }

    override fun getSuggestions(
        prefix: String?,
        sender: CommandSender?,
        namespace: Namespace?,
        mods: MutableList<out Annotation>?
    ): MutableList<String> {
        return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
    }
}