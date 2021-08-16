package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.utils.hasMode
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerParticipationStopEvent
import tc.oc.pgm.flag.Flag
import tc.oc.pgm.flag.event.FlagCaptureEvent
import tc.oc.pgm.flag.event.FlagPickupEvent
import tc.oc.pgm.flag.event.FlagStateChangeEvent
import tc.oc.pgm.flag.state.Captured
import tc.oc.pgm.flag.state.Carried
import tc.oc.pgm.flag.state.Dropped
import java.util.*

class FlagTracker : Listener {

    data class FlagPartial( val id: String, val name: String, val colour: ChatColor, val ownerName: String? )
    data class FlagData( val flagId: String, val playerId: UUID )

    object FlagPickup : OutboundEvent<FlagData>("FLAG_PICKUP")
    object FlagCapture : OutboundEvent<FlagData>("FLAG_CAPTURE")
    object FlagDrop : OutboundEvent<FlagData>("FLAG_DROP")
    object FlagDefend : OutboundEvent<FlagData>("FLAG_DEFEND")

    private val heldFlagCache = mutableMapOf<UUID, Flag>()
    private val lifeLockCache = mutableListOf<UUID>()

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        if (!event.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return

        heldFlagCache.clear()
        lifeLockCache.clear()
    }

    @EventHandler
    fun onParticipantLeave(event: PlayerParticipationStopEvent) {
        if (!event.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return

        heldFlagCache.remove(event.player.id)
        lifeLockCache.remove(event.player.id)
    }

    @EventHandler
    fun onFlagPickup(event: FlagPickupEvent) {
        if (!lifeLockCache.contains(event.carrier.id)) {
            ApiClient.emit(FlagPickup, FlagData(event.flag.id, event.carrier.id))
        }

        heldFlagCache[event.carrier.id] = event.flag
        lifeLockCache.add(event.carrier.id)
    }

    @EventHandler
    fun onFlagDrop(event: FlagStateChangeEvent) {
        if (event.oldState is Carried) {
            val carried = event.oldState as Carried

            if (event.newState is Dropped) {
                ApiClient.emit(FlagDrop, FlagData(event.flag.id, carried.carrier.id))
            } else if (event.newState is Captured) {
                lifeLockCache.remove(carried.carrier.id)
            }
        }
    }

    @EventHandler
    fun onFlagCaptured(event: FlagCaptureEvent) {
        ApiClient.emit(FlagCapture, FlagData(event.goal.id, event.carrier.id))
    }

    @EventHandler
    fun onFlagDefend(event: MatchPlayerDeathEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_FLAG, Gamemode.KING_OF_THE_FLAG)) return

        lifeLockCache.remove(event.victim.id)

        val killer = event.killer ?: return

        val droppedFlag = heldFlagCache[event.victim.id] ?: return

        ApiClient.emit(FlagDefend, FlagData(droppedFlag.id, killer.id))
    }

}