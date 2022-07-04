package network.warzone.mars.player.perks

import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

data class JoinSound(
    val id: String,
    val name: String,
    val sound: String,
    val permission: String,
    val guiIcon: String,
    val guiSlot: Int,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
)

data class JoinSoundParsed(
    val id: String,
    val name: String,
    val bukkitSound: Sound,
    val permission: String,
    val guiIcon: ItemStack,
    val guiSlot: Int,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
)
