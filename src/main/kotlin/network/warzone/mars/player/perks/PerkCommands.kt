package network.warzone.mars.player.perks

import app.ashcon.intake.Command
import app.ashcon.intake.bukkit.parametric.annotation.Sender
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.utils.menu.open
import org.bukkit.entity.Player

class PerkCommands {
    @Command(aliases = ["joinsounds", "joinsound"], desc = "Set a join sound", perms = ["mars.join-sounds"])
    fun onJoinSoundChoose(@Sender sender: Player, playerContext: PlayerContext) {
        sender.open(JoinSoundService.getJoinSoundGUI(playerContext))
    }
}