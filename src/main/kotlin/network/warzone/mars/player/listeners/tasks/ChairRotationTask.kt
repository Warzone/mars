package network.warzone.mars.player.listeners.tasks

import network.warzone.mars.Mars
import network.warzone.mars.player.controllers.SitController
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand
import org.bukkit.scheduler.BukkitRunnable

class ChairRotationTask : BukkitRunnable() {

    val sitController = Mars.sitController

    override fun run() {
        for ((uuid) in sitController.sitting) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            val armorStand = sitController.sitting.get(uuid)?.first ?: continue
            val entityArmorStand = (armorStand as CraftArmorStand).handle
            entityArmorStand.setPositionRotation(
                entityArmorStand.locX,
                entityArmorStand.locY,
                entityArmorStand.locZ,
                player.location.yaw,
                entityArmorStand.pitch
            )
        }
    }

    fun start() {
        this.runTaskTimer(Mars.Companion.get(), 0L, 1L)
    }
}