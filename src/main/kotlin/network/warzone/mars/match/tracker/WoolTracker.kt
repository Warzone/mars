package network.warzone.mars.match.tracker

import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.WoolData
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

class WoolTracker : Listener {
    val holdingCache = mutableMapOf<MonumentWool, MutableList<UUID>>()

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) {
        println(event.match.map.gamemodes)
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val woolMatchModule = event.match.getModule(WoolMatchModule::class.java) ?: return

        holdingCache.clear()

        woolMatchModule.wools.values().forEach {
            holdingCache[it] = mutableListOf()
            println(it.id)
        }
    }

    @EventHandler
    fun onParticipantEnd(event: PlayerParticipationStopEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val wools = holdingCache.filterValues { it.contains(event.player.id) }.keys

        for (wool in wools) {
            ApiClient.emit(OutboundEvent.WoolDrop, WoolData(wool.id, event.player.id))
            holdingCache[wool]?.remove(event.player.id)
        }
    }

    @EventHandler
    fun onWoolPickup(event: GoalTouchEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return
        val wool = event.goal as? MonumentWool ?: return
        val player = event.player ?: return

        ApiClient.emit(OutboundEvent.WoolPickup, WoolData(wool.id, player.id))

        holdingCache[wool]!!.add(player.id)
    }

    @EventHandler
    fun onWoolDrop(event: MatchPlayerDeathEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return

        val wools = holdingCache.filterValues { it.contains(event.victim.id) }.keys

        for (wool in wools) {
            ApiClient.emit(OutboundEvent.WoolDrop, WoolData(wool.id, event.player.id))
            holdingCache[wool]?.remove(event.victim.id)

            if (event.killer != null) {
                val killer = event.killer!!

                ApiClient.emit(OutboundEvent.WoolDefend, WoolData(wool.id, killer.id))
            }
        }
    }

    @EventHandler
    fun onWoolPlaced(event: PlayerWoolPlaceEvent) {
        if (!event.match.hasMode(Gamemode.CAPTURE_THE_WOOL, Gamemode.RACE_FOR_WOOL)) return
        ApiClient.emit(OutboundEvent.WoolCapture, WoolData(event.wool.id, event.player.id))

        holdingCache[event.wool]?.clear()
    }
}