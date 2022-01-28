package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.WoolData
import network.warzone.mars.api.socket.models.WoolDropData
import network.warzone.mars.utils.hasMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.event.MatchStartEvent
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent
import tc.oc.pgm.events.PlayerParticipationStopEvent
import tc.oc.pgm.goals.events.GoalTouchEvent
import tc.oc.pgm.wool.MonumentWool
import tc.oc.pgm.wool.PlayerWoolPlaceEvent
import tc.oc.pgm.wool.WoolMatchModule
import java.util.*

object WoolTracker : Listener {
    val holdingCache: MutableMap<MonumentWool, MutableList<UUID>> = mutableMapOf()

    // Player UUID, Wool ID -> Pickup Time
    private val heldTimeCache: MutableMap<Pair<UUID, String>, Long> = mutableMapOf()

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val woolMatchModule = event.match.getModule(WoolMatchModule::class.java) ?: return

        holdingCache.clear()
        heldTimeCache.clear()

        woolMatchModule.wools.values().forEach {
            holdingCache[it] = mutableListOf()
        }
    }

    @EventHandler
    fun onParticipantEnd(event: PlayerParticipationStopEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val wools = holdingCache.filterValues { it.contains(event.player.id) }.keys

        for (wool in wools) {
            val pickupTime = heldTimeCache[Pair(event.player.id, wool.id)] ?: return println("No pickup time for ${event.player.id} : ${wool.id}")
            val heldTime = Date().time - pickupTime
            ApiClient.emit(OutboundEvent.WoolDrop, WoolDropData(wool.id, event.player.id, heldTime))
            holdingCache[wool]?.remove(event.player.id)
            heldTimeCache.remove(Pair(event.player.id, wool.id))
        }
    }

    @EventHandler
    fun onWoolPickup(event: GoalTouchEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return
        val wool = event.goal as? MonumentWool ?: return
        val player = event.player ?: return

        ApiClient.emit(OutboundEvent.WoolPickup, WoolData(wool.id, player.id))

        holdingCache[wool]!!.add(player.id)
        heldTimeCache[Pair(player.id, wool.id)] = Date().time
    }

    @EventHandler
    fun onWoolDrop(event: MatchPlayerDeathEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val wools = holdingCache.filterValues { it.contains(event.victim.id) }.keys

        for (wool in wools) {
            holdingCache[wool]?.remove(event.victim.id)
            val pickupTime = heldTimeCache[Pair(event.victim.id, wool.id)] ?: return println("No pickup time for ${event.victim.id} : ${wool.id}")
            val heldTime = Date().time - pickupTime
            ApiClient.emit(OutboundEvent.WoolDrop, WoolDropData(wool.id, event.player.id, heldTime))
            heldTimeCache.remove(Pair(event.victim.id, wool.id))

            if (event.killer != null) {
                val killer = event.killer!!

                ApiClient.emit(OutboundEvent.WoolDefend, WoolData(wool.id, killer.id))
            }
        }
    }

    @EventHandler
    fun onWoolPlaced(event: PlayerWoolPlaceEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return
        val player = event.player
        val wool = event.wool

        val pickupTime = heldTimeCache[Pair(player.id, wool.id)] ?: return println("No pickup time for ${player.id} : ${wool.id}")
        val heldTime = Date().time - pickupTime

        ApiClient.emit(OutboundEvent.WoolCapture, WoolDropData(wool.id, player.id, heldTime))

        heldTimeCache.remove(Pair(player.id, event.wool.id))
        holdingCache[wool]?.clear()
    }
}