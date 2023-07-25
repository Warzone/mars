package network.warzone.mars.player.achievements

import network.warzone.mars.Mars
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

interface AchievementAgent : Listener {
    fun load() {
        Mars.registerEvents(this)
    }
    fun unload() {
        HandlerList.unregisterAll(this)
    }
}





























