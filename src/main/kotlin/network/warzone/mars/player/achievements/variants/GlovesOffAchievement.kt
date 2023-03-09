package network.warzone.mars.player.achievements.variants

import kotlinx.coroutines.runBlocking
import net.minecraft.server.v1_8_R3.ItemStack
import net.minecraft.server.v1_8_R3.Material
import network.warzone.mars.Mars
import network.warzone.mars.achievement.Achievement
import network.warzone.mars.achievement.AchievementAgent
import network.warzone.mars.achievement.AchievementEmitter
import network.warzone.mars.player.PlayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent

object GlovesOffAchievement {
    fun createGlovesOffAchievement(achievement: Achievement, titleName: String) : AchievementAgent =
        object : AchievementAgent, Listener {
            override var match: Match? = null
            override val title: String = titleName
            override val description: String = "Kill a player with only your fists, while wearing no armor."
            override val gamemode: String = "NONE"

            override fun load() {
                //Mars.registerEvents(this)
            }

            // TODO: Fix possible bug
            // I'm pretty sure with the way this code is set up, players will be able to simply knock
            // somebody into the void and quickly take off all their armor and switch their held slot
            // to air, since this event only fires upon the player's death. To fix this, there would
            // probably have to be some system that checks for their inventory/held slot every hit,
            // and if on the final hit they were wearing no armor and using a fist, it can emit.
            @EventHandler
            fun onPlayerDeath(event: MatchPlayerDeathEvent) = runBlocking {
                val killer = event.killer ?: return@runBlocking
                val eventInventory = killer.player.get().inventory

                println("killer = $killer")
                println("eventInventory =  $eventInventory")
                println("eventInventory.itemInHand.data.itemType.equals(Material.AIR) = " +
                        eventInventory.itemInHand.data.itemType.equals(Material.AIR))
                println("eventInventory.itemInHand.data.itemType = \\" +
                        eventInventory.itemInHand.data.itemType)

                for (armorSlot in eventInventory.armorContents) {
                    println("armorSlot = $armorSlot")
                }



                if (!eventInventory.itemInHand.data.itemType.equals(Material.AIR)) return@runBlocking


                for (armorSlot in eventInventory.armorContents) {
                    if (!armorSlot.equals(Material.AIR)) return@runBlocking
                }

                AchievementEmitter.emit(killer.player.get(), achievement)
            }

            override fun unload() {
                //HandlerList.unregisterAll(this)
            }
        }
}