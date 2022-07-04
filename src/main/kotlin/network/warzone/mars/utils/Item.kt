package network.warzone.mars.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

fun getHead(playerName: String): ItemStack {
    val item = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
    val skull = item.itemMeta as SkullMeta
    skull.displayName = playerName
    skull.owner = playerName
    item.itemMeta = skull
    return item
}

object ItemUtils {
    private val materialNames: List<String> = Material.values().map { it.name }

    fun getMaterialByName(name: String) : Material? {
        val formattedMaterialName = name.enumify()
        if (formattedMaterialName !in materialNames) return null
        return Material.valueOf(formattedMaterialName)
    }
}