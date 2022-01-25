package network.warzone.mars.match

import io.ktor.util.reflect.*
import network.warzone.mars.Mars
import network.warzone.mars.match.tracker.*
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import kotlin.reflect.KClass

object MatchManager {
    val trackers: List<Listener> = listOf(
        CoreTracker(),
        MatchTracker(),
        PlayerTracker(),
        KillstreakTracker(),
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