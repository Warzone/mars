package network.warzone.mars.player

import network.warzone.mars.Mars
import network.warzone.mars.player.level.LevelDisplayListener
import network.warzone.mars.player.listeners.ChatListener
import network.warzone.mars.player.listeners.InventoryListener
import network.warzone.mars.player.models.Session
import network.warzone.mars.punishment.models.Punishment
import org.bukkit.entity.Player
import java.util.*

object PlayerManager {
    private val players: MutableMap<UUID, PlayerContext> = HashMap()

    init {
        Mars.registerEvents(ChatListener())
        Mars.registerEvents(InventoryListener())
        Mars.registerEvents(LevelDisplayListener())
    }

    fun createPlayer(player: Player, session: Session, activePunishments: List<Punishment>): PlayerContext {
        return PlayerContext(
            uuid = player.uniqueId,
            player = player,
            activeSession = session,
            activePunishments = activePunishments
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