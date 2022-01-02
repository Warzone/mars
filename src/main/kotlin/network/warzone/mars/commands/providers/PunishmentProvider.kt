package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.ArgumentParseException
import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import kotlinx.coroutines.runBlocking
import network.warzone.mars.punishment.PunishmentFeature
import network.warzone.mars.punishment.exceptions.PunishmentMissingException
import network.warzone.mars.punishment.models.Punishment
import org.bukkit.command.CommandSender
import java.util.*

class PunishmentProvider : BukkitProvider<Punishment> {
    override fun getName(): String {
        return "punishment"
    }

    override fun get(sender: CommandSender?, args: CommandArgs, annotations: MutableList<out Annotation>?): Punishment =
        runBlocking {
            val punId = UUID.fromString(args.next())

            val punishment: Punishment? = PunishmentFeature.get(punId)
            punishment ?: throw ArgumentParseException(
                PunishmentMissingException(punId.toString()).asTextComponent().content()
            )

            return@runBlocking punishment
        }
}
