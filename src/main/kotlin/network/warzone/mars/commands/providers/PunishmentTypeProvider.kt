package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.argument.MissingArgumentException
import app.ashcon.intake.argument.Namespace
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import network.warzone.mars.punishment.PunishmentFeature
import org.bukkit.command.CommandSender


class PunishmentTypeProvider : BukkitProvider<String> {
    override fun getName(): String {
        return "punishmentType"
    }

    override fun get(sender: CommandSender, args: CommandArgs, annotations: MutableList<out Annotation>?): String? {
        return readGreedyString(args)
    }

    override fun getSuggestions(
        prefix: String,
        sender: CommandSender,
        namespace: Namespace,
        mods: MutableList<out Annotation>
    ): List<String> {

        val query = prefix.toLowerCase()

        return PunishmentFeature.punishmentTypes
            .filter { it.requiredPermission == null || sender.hasPermission(it.requiredPermission) }
            .map { it.short }
            .filter { it.startsWith(query, ignoreCase = true) }
    }

    // Stripped from https://github.com/Electroid/intake/blob/master/core/src/main/java/app/ashcon/intake/parametric/provider/TextProvider.java
    private fun readGreedyString(arguments: CommandArgs): String {
        val builder = StringBuilder()
        var first = true
        while (true) {
            if (!first) {
                builder.append(" ")
            }
            try {
                builder.append(arguments.next())
            } catch (ignored: MissingArgumentException) {
                break
            }
            first = false
        }
        if (first) {
            throw MissingArgumentException()
        }
        return builder.toString().trim { it <= ' ' }
    }

}
