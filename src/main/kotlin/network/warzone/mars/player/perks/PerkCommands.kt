package network.warzone.mars.player.perks

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.utils.menu.open
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PerkCommands {
    @Command(aliases = ["sounds"], desc = "Set a join sound")
    fun onJoinSoundChoose(@Sender sender: CommandSender) {
        if (sender !is Player) return
        sender.open(JoinSoundService.getJoinSoundGUI(sender))
    }
}