package network.warzone.mars.match.tracker

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

/*
* Tracker for stats that are sent at end of match (PGM tracks most of the stats itself like Bow Shots etc.)
*/
class BigStatsTracker : Listener {
    val blockCache = HashMap<UUID, PlayerBlocks>()
    val offlinePlayersPendingStatSave = mutableSetOf<UUID>()
    lateinit var matchStatsModule: StatsMatchModule

    @EventHandler
    fun onMatchLoad(event: MatchLoadEvent) {
        offlinePlayersPendingStatSave.clear()
        matchStatsModule = event.match.getModule(StatsMatchModule::class.java)!!
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val blockType = event.blockPlaced.type
        val playerStats = blockCache[event.player.uniqueId] ?: PlayerBlocks()
        var count = playerStats.blocksPlaced[blockType] ?: 0
        count++
        playerStats.blocksPlaced[blockType] = count
        blockCache[event.player.uniqueId] = playerStats
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val blockType = event.block.type
        val playerStats = blockCache[event.player.uniqueId] ?: PlayerBlocks()
        var count = playerStats.blocksBroken[blockType] ?: 0
        count++
        playerStats.blocksBroken[blockType] = count
        blockCache[event.player.uniqueId] = playerStats
    }

    @EventHandler
    fun onMatchEnd(event: MatchFinishEvent) {
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