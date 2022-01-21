package network.warzone.mars.utils

import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.match.MatchManager as MarsMatchManager
import org.bukkit.entity.Player
import tc.oc.pgm.api.map.Gamemode
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.match.MatchManager
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.player.ParticipantState

fun Match.hasMode(vararg gamemodes: Gamemode): Boolean {
    return this.map.gamemodes.any {
        gamemodes.contains(it)
    }
}

fun MatchManager.getMatch(): Match {
    return this.matches.asSequence().toList().maxByOrNull { it.players.size }!!
}

val MatchPlayer.simple: SimplePlayer
    get() = SimplePlayer(this.id, this.nameLegacy)

val ParticipantState.simple: SimplePlayer
    get() = SimplePlayer(this.id, this.nameLegacy)

val Player.matchPlayer: MatchPlayer
    get() = MarsMatchManager.match.getPlayer(this) ?: throw RuntimeException("Player has no MatchPlayer? ${this.name}")