package network.warzone.pgm.match.tracker

import network.warzone.pgm.api.socket.models.ChatChannel
import network.warzone.pgm.match.MatchManager
import network.warzone.pgm.player.listeners.ChatListener
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import tc.oc.pgm.api.match.event.MatchFinishEvent
import tc.oc.pgm.api.player.ParticipantState
import tc.oc.pgm.tracker.TrackerMatchModule
import java.util.*


data class PlayerBlocks(var blocksPlaced: EnumMap<Material, Int> = EnumMap(Material::class.java), var blocksBroken: EnumMap<Material, Int> = EnumMap(Material::class.java))

/*
* Tracker for stats that are sent at end of match (PGM tracks most of the stats itself like Bow Shots etc.)
*/
class BigStatsTracker : Listener {
    val blockCache = HashMap<UUID, PlayerBlocks>()
    val messageCache = HashMap<UUID, EnumMap<ChatChannel, Int>>()

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
        val playerStats = messageCache[event.matchPlayer.id] ?: EnumMap<ChatChannel, Int>(ChatChannel::class.java)
        var count = playerStats[event.channel] ?: 0
        count++
        playerStats[event.channel] = count
        messageCache[event.matchPlayer.id] = playerStats
    }

    @EventHandler
    fun onMatchEnd(event: MatchFinishEvent) {
        messageCache.clear()
        blockCache.clear()
    }
}