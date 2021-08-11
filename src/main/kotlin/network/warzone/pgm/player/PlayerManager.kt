package network.warzone.pgm.player

import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.player.listeners.ChatListener
import network.warzone.pgm.player.models.Session
import org.bukkit.entity.Player
import java.util.*

object PlayerManager {

    private val players: MutableMap<UUID, PlayerContext> = HashMap()

    init {
        WarzonePGM.registerEvents(ChatListener())
    }

    fun createPlayer(player: Player, session: Session): PlayerContext {
        return PlayerContext(
            uuid = player.uniqueId,
            player = player,
            activeSession = session
        ).also { players[it.uuid] = it }
    }

    fun addPlayer(context: PlayerContext) {
        players[context.uuid] = context
    }

    fun getPlayer(username: String): PlayerContext? {
        return players.values.firstOrNull { it.player.name.equals(username, ignoreCase = true) }
    }

    fun getPlayer(uuid: UUID): PlayerContext? {
        return players[uuid]
    }

    fun removePlayer(uuid: UUID): PlayerContext? {
        return players.remove(uuid)
    }

}