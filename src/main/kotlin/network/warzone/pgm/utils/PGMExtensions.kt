package network.warzone.pgm.utils

import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.MatchManager

fun Match.hasMode(vararg gamemodes: Gamemode): Boolean {
    return this.map.gamemodes.any {
        gamemodes.contains(it)
    }
}

fun MatchManager.getMatch(): Match {
    return this.matches.asSequence().toList().maxByOrNull { it.players.size }!!
}