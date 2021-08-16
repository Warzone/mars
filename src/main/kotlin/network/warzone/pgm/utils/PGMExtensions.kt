package network.warzone.pgm.utils

import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match

fun Match.hasMode(vararg gamemodes: Gamemode): Boolean {
    return this.map.gamemodes.any {
        gamemodes.contains(it)
    }
}