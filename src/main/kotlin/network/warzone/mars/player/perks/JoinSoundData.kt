package network.warzone.mars.player.perks

import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

data class JoinSoundData(
    val id: String,
    val name: String,
    val description: List<String> = emptyList(),
    val sound: String,
    val permission: String,
    val guiIcon: String,
    val guiSlot: Int,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
)

data class JoinSound(
    val id: String,
    val name: String,
    val description: List<String> = emptyList(),
    val bukkitSound: Sound,
    val permission: String,
    val guiIcon: ItemStack,
    val guiSlot: Int,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
)
