package network.warzone.mars.match.tracker

import network.warzone.mars.api.socket.models.ChatChannel
import network.warzone.mars.player.listeners.ChatListener
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.match.event.MatchLoadEvent
import tc.oc.pgm.events.PlayerJoinMatchEvent
import tc.oc.pgm.events.PlayerLeaveMatchEvent
import tc.oc.pgm.stats.StatsMatchModule
import java.util.*
import kotlin.collections.HashMap


data class PlayerBlocks(
    var blocksPlaced: EnumMap<Material, Int> = EnumMap(Material::class.java),
    var blocksBroken: EnumMap<Material, Int> = EnumMap(Material::class.java)
)

data class PlayerMessages(var staff: Int = 0, var global: Int = 0, var team: Int = 0)

/*
* Tracker for stats that are sent at end of match (PGM tracks most of the stats itself like Bow Shots etc.)
*/
class BigStatsTracker : Listener {
    val blockCache = HashMap<UUID, PlayerBlocks>()
    val messageCache = HashMap<UUID, PlayerMessages>()
    val offlinePlayersPendingStatSave = mutableSetOf<UUID>()
    var matchStatsModule: StatsMatchModule? = null

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) {
        offlinePlayersPendingStatSave.clear()
        matchStatsModule = event.match.getModule(StatsMatchModule::class.java)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val blockType = event.blockPlaced.type
        val playerStats = blockCache[event.player.uniqueId] ?: PlayerBlocks()
        var count = playerStats.blocksPlaced[blockType] ?: 0;
        count++;
        playerStats.blocksPlaced[blockType] = count;
        blockCache[event.player.uniqueId] = playerStats
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val blockType = event.block.type
        val playerStats = blockCache[event.player.uniqueId] ?: PlayerBlocks()
        var count = playerStats.blocksBroken[blockType] ?: 0;
        count++;
        playerStats.blocksBroken[blockType] = count;
        blockCache[event.player.uniqueId] = playerStats
    }

    @EventHandler
    fun onPlayerChat(event: ChatListener.MatchPlayerChatEvent) {
        val messageStats = messageCache[event.matchPlayer.id] ?: PlayerMessages()
        when (event.channel) {
            ChatChannel.STAFF -> messageStats.staff++
            ChatChannel.GLOBAL -> messageStats.global++
            ChatChannel.TEAM -> messageStats.team++
        }
        messageCache[event.matchPlayer.id] = messageStats
    }

    @EventHandler
    fun onMatchEnd(event: MatchFinishEvent) {
        messageCache.clear()
        blockCache.clear()
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerLeaveMatchEvent) {
        offlinePlayersPendingStatSave.add(event.player.id)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinMatchEvent) {
        if (offlinePlayersPendingStatSave.contains(event.player.id)) offlinePlayersPendingStatSave.remove(event.player.id)
    }
}