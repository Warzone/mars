package network.warzone.mars.match.tracker

import network.warzone.mars.Mars
import network.warzone.mars.match.MatchManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.match.MatchPhase
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.rotation.MapPoolManager

// A tracker to ensure the Droplet rotation is cycled to under proper conditions.
class MapPoolTracker : Listener {
    var mapCycleTask: BukkitTask? = null
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (MatchManager.match.phase != MatchPhase.IDLE) return
        val playerCount = Bukkit.getServer().onlinePlayers.size
        cycleTask(playerCount)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        if (MatchManager.match.phase != MatchPhase.IDLE) return
        // Need the -1 because it seems the online player count
        // is not updated before this event's code is called.
        val playerCount = Bukkit.getServer().onlinePlayers.size - 1
        cycleTask(playerCount)
    }

    // Cycles the map when the player count goes from 2 -> 1 or 0 -> 1.
    // Is ignored if the active map is from the Droplet cycle.
    private fun cycleTask(playerCount: Int) {
        val currentPool: MapPoolManager = PGM.get().mapOrder as MapPoolManager
        if (currentPool.activeMapPool.name == "Droplet") return
        if (playerCount == 1) {
            mapCycleTask?.cancel()
            mapCycleTask = Bukkit.getScheduler().runTaskLater(Mars.instance, Runnable {
                MatchManager.match.callEvent(MatchFinishEvent(MatchManager.match, MatchManager.match.competitors))
            }, 20L * 10)  // 10 seconds
        } else {
            // If player count is 2 or more, cancel the task
            mapCycleTask = null
        }
    }
}