package network.warzone.pgm.match

import network.warzone.pgm.WarzonePGM

object MatchManager {

    fun init() {
        WarzonePGM.registerEvents(MatchListener())
    }

}