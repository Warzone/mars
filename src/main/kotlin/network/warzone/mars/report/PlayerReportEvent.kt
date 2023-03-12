package network.warzone.mars.report

import network.warzone.mars.utils.KEvent
import org.bukkit.event.Cancellable
import tc.oc.pgm.api.player.MatchPlayer

class PlayerReportEvent(
    val player: MatchPlayer,
    val sender: MatchPlayer,
    val reason: String,
    var canceled: Boolean = false
) : KEvent(), Cancellable {

    override fun isCancelled(): Boolean = this.canceled

    override fun setCancelled(canceled: Boolean) {
        this.canceled = canceled
    }

}