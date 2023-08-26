package network.warzone.mars.player.achievements

import network.warzone.mars.Mars
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.event.MatchFinishEvent

interface AchievementAgent : Listener {
    val emitter : AchievementEmitter
    fun load() {
        Mars.registerEvents(this)
    }
    fun unload() {
        HandlerList.unregisterAll(this)
    }
}





























