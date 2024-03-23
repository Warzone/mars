package network.warzone.mars.match

import io.ktor.util.reflect.*
import network.warzone.mars.Mars
import network.warzone.mars.match.tracker.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.MatchPhase
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.rotation.MapPoolManager
import kotlin.reflect.KClass

object MatchManager {

    val trackers: List<Listener> = listOf(
        CoreTracker(),
        MatchTracker(),
        PlayerTracker(),
        KillstreakTracker,
        WoolTracker,
        FlagTracker(),
        DestroyableTracker(),
        ControlPointTracker(),
        ChatTracker(),
        BigStatsTracker()
    )

    inline fun <reified T : Listener> getTracker(tracker: KClass<T>): T? {
        return trackers.find { it.instanceOf(tracker) } as T?
    }

    lateinit var match: Match

    fun init() {
        trackers.forEach(Mars::registerEvents)
        Mars.registerEvents(ObjectiveAnnouncer())
    }
}