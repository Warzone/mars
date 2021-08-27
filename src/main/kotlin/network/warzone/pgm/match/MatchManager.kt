package network.warzone.pgm.match

import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.match.tracker.*
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match

object MatchManager {

    private val trackers: List<Listener> = listOf(
        CoreTracker(),
        MatchTracker(),
        PlayerTracker(),
        WoolTracker(),
        FlagTracker(),
        DestroyableTracker(),
        ControlPointTracker(),
        ChatTracker()
    )

    lateinit var match: Match

    fun init() {
        trackers.forEach(WarzonePGM::registerEvents)
    }

}