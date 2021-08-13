package network.warzone.pgm.match

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.socket.MatchLoadData
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.map.MapFeature
import network.warzone.pgm.map.models.GameMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.api.match.event.MatchStartEvent

class MatchListener : Listener {

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) = runBlocking {
        val gameMap: GameMap = MapFeature.getKnown(event.match.map.name)

        WarzonePGM.get().apiClient.emit(
            OutboundEvent.MatchLoad,
            MatchLoadData(gameMap._id)
        )
    }

    @EventHandler
    fun onMatchStart(event: MatchStartEvent) = runBlocking {
        WarzonePGM.get().apiClient.emit(
            OutboundEvent.MatchStart,
            Unit
        )
    }

}