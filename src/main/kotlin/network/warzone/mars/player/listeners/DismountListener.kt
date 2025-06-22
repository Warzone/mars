package network.warzone.mars.player.listeners

import net.minecraft.server.v1_8_R3.BlockPosition
import net.minecraft.server.v1_8_R3.World
import network.warzone.mars.Mars
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.spigotmc.event.entity.EntityDismountEvent
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class DismountListener : Listener {

    val sitController = Mars.Companion.sitController

    @EventHandler
    fun onDismount(event: EntityDismountEvent) {
        if (event.entity !is Player && event.dismounted !is ArmorStand) {
            return
        } else {
            val player = event.entity as Player;
            val seat = event.dismounted as ArmorStand
            sitController.unSit(player, seat);
            println(sitController.sitting)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (sitController.isSitting(event.player)) {
            sitController.unSit(event.player)
        }
    }

    @EventHandler
    fun onPlayerDamaged(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        sitController.unSit(player);
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        for ((uuid, armorStand) in sitController.sitting) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            if (armorStand == null || !player.isOnline) continue
            if (block == getBlockBelow(player)) {
                sitController.unSit(player)
            }
        }
    }

    private fun getBlockBelow(entity: Entity): Block? {
        val bb = (entity as CraftEntity).handle.boundingBox
        val minX = floor(bb.a)
        val maxX = floor(bb.d)
        val minZ = floor(bb.c)
        val maxZ = floor(bb.f)
        val y = floor(bb.b - 0.01)
        val world = entity.world
        for (x in minX.toInt()..maxX.toInt()) {
            for (z in minZ.toInt()..maxZ.toInt()) {
                val loc = Location(world, x.toDouble(),y, z.toDouble())
                val block = world.getBlockAt(loc)
                if (block.type == Material.AIR) {
                    val belowBlock = world.getBlockAt(loc.subtract(0.0, 1.0, 0.0))
                    val type = belowBlock.type
                    if (type == Material.FENCE ||
                        type == Material.NETHER_FENCE ||
                        type == Material.COBBLE_WALL ||
                        type == Material.STEP ||
                        type == Material.FENCE_GATE) {
                        return belowBlock
                    }
                }
                return block
            }
        }
        return null;
    }

}