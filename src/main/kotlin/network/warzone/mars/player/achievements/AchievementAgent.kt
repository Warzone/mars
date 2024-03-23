package network.warzone.mars.player.achievements

import network.warzone.mars.Mars
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

interface AchievementAgent : Listener {
    val emitter : AchievementEmitter
    fun load() {
        Mars.registerEvents(this)
    }
    fun unload() {
        HandlerList.unregisterAll(this)
    }
}





























