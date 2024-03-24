package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.FlagCaptureData
import network.warzone.mars.api.socket.models.FlagDefendData
import network.warzone.mars.api.socket.models.FlagDropData
import network.warzone.mars.api.socket.models.FlagPickupData
import network.warzone.mars.utils.hasMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerParticipationStopEvent
import tc.oc.pgm.flag.Flag
import tc.oc.pgm.flag.event.FlagPickupEvent
import tc.oc.pgm.flag.event.FlagStateChangeEvent
import tc.oc.pgm.flag.state.Captured
import tc.oc.pgm.flag.state.Carried
import tc.oc.pgm.flag.state.Dropped
import java.util.*

class FlagTracker : Listener {
    private val heldFlagCache = mutableMapOf<UUID, Flag>()
    private val heldTimeCache = mutableMapOf<UUID, Long>()
    private val lifeLockCache = mutableListOf<UUID>()

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        if (!event.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return

        heldFlagCache.clear()
        heldTimeCache.clear()
        lifeLockCache.clear()
    }

    @EventHandler
    fun onParticipantLeave(event: PlayerParticipationStopEvent) {
        if (!event.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return

        if (heldFlagCache.contains(event.player.id)) {
            val flag = heldFlagCache[event.player.id]!!

            val pickupTime = heldTimeCache[event.player.id]!!
            val heldTime = Date().time - pickupTime

            ApiClient.emit(OutboundEvent.FlagDrop, FlagDropData(flag.id, event.player.id, heldTime))
        }

        remove(event.player.id)
    }

    @EventHandler
    fun onFlagPickup(event: FlagPickupEvent) {
        if (!event.flag.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return

        if (!lifeLockCache.contains(event.carrier.id)) {
            ApiClient.emit(OutboundEvent.FlagPickup, FlagPickupData(event.flag.id, event.carrier.id))
        }

        heldFlagCache[event.carrier.id] = event.flag
        heldTimeCache[event.carrier.id] = Date().time
        lifeLockCache.add(event.carrier.id)
    }

    @EventHandler
    fun onFlagDrop(event: FlagStateChangeEvent) {
        if (!event.match.hasMode(Gamemode.KING_OF_THE_FLAG, Gamemode.CAPTURE_THE_FLAG)) return
        if (event.oldState is Carried) {
            val carried = event.oldState as Carried

            val pickupTime = heldTimeCache[carried.carrier.id]!!
            val heldTime = Date().time - pickupTime

            heldFlagCache.remove(carried.carrier.id)
            heldTimeCache.remove(carried.carrier.id)

            if (event.newState is Dropped) {
                ApiClient.emit(OutboundEvent.FlagDrop, FlagDropData(event.flag.id, carried.carrier.id, heldTime))
            } else if (event.newState is Captured) {
                lifeLockCache.remove(carried.carrier.id)

                ApiClient.emit(OutboundEvent.FlagCapture, FlagCaptureData(event.flag.id, carried.carrier.id, heldTime))
            }
        }
    }

    @EventHandler
    fun onFlagDefend(event: MatchPlayerDeathEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_FLAG, Gamemode.KING_OF_THE_FLAG)) return

        val killer = event.killer ?: return

        val droppedFlag = heldFlagCache[event.victim.id] ?: return

        lifeLockCache.remove(event.victim.id)

        ApiClient.emit(OutboundEvent.FlagDefend, FlagDefendData(droppedFlag.id, killer.id))
    }

    private fun remove(playerId: UUID) {
        lifeLockCache.remove(playerId)
        heldTimeCache.remove(playerId)
        heldFlagCache.remove(playerId)
    }
}