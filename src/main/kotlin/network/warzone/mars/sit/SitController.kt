package network.warzone.mars.sit

import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.UUID

class SitController {
    val sitting: HashSet<UUID?> = HashSet()

    fun sit(player: Player) {
        if (isSitting(player)) {
            return;
        }

        sitting.add(player.uniqueId)
        createChair(player.location).passenger = player;
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
        seat.remove()
        sitting.remove(player.uniqueId)


    }


    private fun createChair(loc: Location): ArmorStand {
        val world = loc.world
        val chair = world.spawnEntity(loc.subtract(0.0, 0.6, 0.0), EntityType.ARMOR_STAND) as ArmorStand
        chair.setGravity(false)
        chair.isMarker = true;
        return chair
    }
}