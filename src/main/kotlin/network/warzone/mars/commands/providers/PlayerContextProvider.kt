package network.warzone.mars.commands.providers

import app.ashcon.intake.argument.CommandArgs
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider
import app.ashcon.intake.parametric.ProvisionException
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PlayerContextProvider : BukkitProvider<PlayerContext> {
    override fun get(sender: CommandSender, args: CommandArgs?, list: MutableList<out Annotation>?): PlayerContext {
        if (sender is Player) return PlayerManager.getPlayer(sender.uniqueId)!!
        else throw ProvisionException("You must be a player to run this command.")
    }

    override fun isProvided(): Boolean {
        return true
    }
}