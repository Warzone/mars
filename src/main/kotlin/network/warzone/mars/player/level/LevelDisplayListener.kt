package network.warzone.mars.player.level

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.match.tracker.PlayerXPGainEvent
import network.warzone.mars.player.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitTask
import tc.oc.pgm.api.match.event.MatchUnloadEvent
import tc.oc.pgm.spawns.events.PlayerSpawnEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

class LevelDisplayListener : Listener {

    companion object {
        val EXP_INVENTORIES = arrayOf(
            InventoryType.ANVIL,
            InventoryType.ENCHANTING
        )
        const val METADATA_KEY = "vanilla-experience"
    }

    private val showUntil: ConcurrentHashMap<Player, Long> = ConcurrentHashMap()
    private var task: BukkitTask? = null

    init {
        this.task = Bukkit.getScheduler().runTaskTimer(Mars.get(), {
            val date = Date().time
            showUntil.forEach {
                if (it.value <= date) { // Return to stats level
                    // Store real XP before showing stats level
                    setVanillaExpInMetadata(it.key, getExactLevel(it.key))
                    showStatsLevel(it.key)
                    showUntil.remove(it.key)
                }
            }
        }, 1L, 1L)
    }

    private fun getExactLevel(player: Player): Float {
        return player.level + player.exp
    }

    private fun setExactLevel(player: Player, level: Float) {
        player.level = floor(level.toDouble()).toInt()
        player.exp = level - player.level
    }

    private fun getVanillaExpFromMetadata(player: Player): Float {
        return player.getMetadata(METADATA_KEY, Mars.get()).asFloat()
    }

    private fun setVanillaExpInMetadata(player: Player, level: Float) {
        player.setMetadata(METADATA_KEY, FixedMetadataValue(Mars.get(), level))
    }

    // Mars stat system level
    private fun showStatsLevel(player: Player) = runBlocking {
        val context = PlayerManager.getPlayer(player.uniqueId)
        val profile = context?.getPlayerProfile()
        val stats = profile?.stats
        player.level = stats?.level ?: 1
        player.exp = (stats?.xp?.rem(5000F))?.div(5000F) ?: 0F
    }

    // Vanilla experience level
    private fun showVanillaLevel(player: Player, seconds: Long) {
        val level = getVanillaExpFromMetadata(player)
        setExactLevel(player, level)
        showUntil[player] = if (seconds == Long.MAX_VALUE) Long.MAX_VALUE else Date().time + seconds * 1000
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        showUntil.remove(event.player)
    }

    @EventHandler
    fun onSpawn(event: PlayerSpawnEvent) {
        // Get real XP when player respawns in case it has been reset
        setVanillaExpInMetadata(event.player.bukkit, getExactLevel(event.player.bukkit))
        showStatsLevel(event.player.bukkit)
    }

    @EventHandler
    fun openInventory(event: InventoryOpenEvent) {
        if (EXP_INVENTORIES.contains(event.inventory.type)) {
            if (showUntil.contains(event.actor)) {
                // Store real XP in case it has changed
                setVanillaExpInMetadata(event.actor, getExactLevel(event.actor))
            }
            // Mark as showing real XP indefinitely (until inventory is closed)
            showVanillaLevel(event.actor, Long.MAX_VALUE)
        }
    }

    @EventHandler
    fun closeInventory(event: InventoryCloseEvent) {
        if (EXP_INVENTORIES.contains(event.inventory.type)) {
            if (showUntil.contains(event.actor)) {
                // Store current real XP in case experience has been used (enchantment table or anvil)
                setVanillaExpInMetadata(event.actor, getExactLevel(event.actor))
            }
            // Mark as showing real XP for 5 seconds
            showVanillaLevel(event.actor, 5L)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onExpChange(event: PlayerExpChangeEvent) {
        // Get real XP from metadata
        val level = getVanillaExpFromMetadata(event.player)
        // Show real XP
        setExactLevel(event.player, level)
        // Give XP from event
        event.player.giveExp(event.amount)
        // Store current real XP in player metadata
        setVanillaExpInMetadata(event.player, getExactLevel(event.player))
        // Mark as showing real XP for 5 seconds
        showVanillaLevel(event.player, 5L)
        // Cancel real event
        event.amount = 0
    }

    @EventHandler
    fun onXP(event: PlayerXPGainEvent) {
        val player: Player? = Bukkit.getPlayer(event.data.playerId)
        if (player == null || showUntil.contains(player)) return
        showStatsLevel(player)
    }

    @EventHandler
    fun onCycle(event: MatchUnloadEvent) {
        showUntil.clear()
    }

}