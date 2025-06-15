package network.warzone.mars.sit

import app.ashcon.intake.CommandException
import network.warzone.mars.Mars
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.UUID

class SitController {
    val sitting: HashMap<UUID?, ArmorStand?> = HashMap()

    fun sit(player: Player) {
        if (isSitting(player)) {
            return;
        }

        if(!validSeatLocation(player)) {
            throw CommandException("Invalid sitting location")
        }

        val chair = createChair(player.location, player)
        sitting.put(player.uniqueId, chair)

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
        sitting.remove(player.uniqueId)
        seat.remove()
    }

    fun validSeatLocation(player: Player): Boolean {
        return !(player.isFlying || player.isSleeping || !player.isValid)

    }


    private fun createChair(loc: Location, player: Player): ArmorStand {
        val world = loc.world
        val chair = world.spawnEntity(loc.subtract(0.0, 0.0, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        chair.setGravity(false)
        chair.isMarker = true
        chair.isSmall = true
        chair.passenger = player
        return chair
    }
}