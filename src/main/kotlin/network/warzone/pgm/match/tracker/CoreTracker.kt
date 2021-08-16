package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.match.models.Contribution
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.core.CoreBlockBreakEvent
import tc.oc.pgm.core.CoreLeakEvent
import java.util.*
import kotlin.math.floor

class CoreTracker : Listener {

    data class CorePartial( val id: String, val name: String, val ownerName: String, val material: Material )
    data class CoreDamageData(val coreId: String, val playerId: UUID)
    data class CoreLeakData(val coreId: String, val contributions: List<Contribution>)

    object CoreDamage : OutboundEvent<CoreDamageData>("CORE_DAMAGE")
    object CoreLeak : OutboundEvent<CoreLeakData>("CORE_LEAK")

    @EventHandler
    fun onCoreDamage(event: CoreBlockBreakEvent) {
        ApiClient.emit(CoreDamage, CoreDamageData(event.core.id, event.player.id))
    }

    @EventHandler
    fun onCoreLeak(event: CoreLeakEvent) {
        val totalBrokenBlocks = event.core.casingRegion.blocks.filter { !event.core.isObjectiveMaterial(it) }.size

        ApiClient.emit(CoreLeak, CoreLeakData(
            event.core.id,
            event.core.contributions
                .map { Contribution(
                    it.playerState.id,
                    it.percentage.toFloat(),
                    floor(it.percentage * totalBrokenBlocks).toInt()
                ) }
        ))
    }

}