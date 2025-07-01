package network.warzone.mars.player.controllers

import app.ashcon.intake.CommandException
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.floor

class SitController {
    val sitting: HashMap<UUID?, Pair<ArmorStand?, Block?>> = HashMap()

    fun sit(player: Player) {
        if (isSitting(player)) {
            return;
        }
        val block = getBlockBelow(player)
        if(!validSeatLocation(player, block)) {
            throw CommandException("You were unable to sit!")
        }
        val chair = createChair(player.location, player)
        sitting.put(player.uniqueId, Pair(chair, block))
        player.sendActionBar("${ChatColor.GREEN}You are now sitting!")
    }

    fun isSitting(player: Player): Boolean {
        return sitting.contains(player.uniqueId);
    }

    fun unSit(player: Player) {
        if (player.vehicle is ArmorStand) {
            unSit(player, player.vehicle as ArmorStand)
        }
    }

    fun unSit(player: Player, seat: ArmorStand) {
        if (!isSitting(player)) {
            return;
        }
        player.sendActionBar("${ChatColor.DARK_RED}You are no longer sitting!")
        sitting.remove(player.uniqueId)
        seat.remove()
    }

    fun validSeatLocation(player: Player, block: Block?): Boolean {
        return !(block?.type == Material.AIR || block == null || !player.isOnGround);
    }

    fun clearAllSeats() {
        for ((uuid) in sitting) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            unSit(player)
        }
    }

    private fun createChair(loc: Location, player: Player): ArmorStand {
        val world = loc.world
        val chair = world.spawnEntity(loc.add(0.0, 0.05, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        chair.setGravity(false)
        chair.isMarker = true
        chair.isSmall = true
        chair.passenger = player
        chair.isVisible = false
        return chair
    }


    fun getBlockBelow(entity: Entity): Block? {
        val bb = (entity as CraftEntity).handle.boundingBox
        val minX = floor(bb.a)
        val maxX = floor(bb.d)
        val minZ = floor(bb.c)
        val maxZ = floor(bb.f)
        val y = floor(bb.b - 0.02)
        val world = entity.world
        for (x in minX.toInt()..maxX.toInt()) {
            for (z in minZ.toInt()..maxZ.toInt()) {
                val loc = Location(world, x.toDouble(), y, z.toDouble())
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
                } else {
                    return block
                }
            }
        }
        return null;
    }
}