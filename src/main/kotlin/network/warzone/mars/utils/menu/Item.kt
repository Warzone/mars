package network.warzone.mars.utils.menu

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun item(
    type: Material = Material.STAINED_GLASS,
    builder: Item.() -> Unit = {}
) = Item(type).apply(builder)

class Item(
    val type: Material
) {
    var stack = ItemStack(type, 1)
    val meta: ItemMeta get() = stack.itemMeta

    fun stack(builder: ItemStack.() -> Unit) = stack
        .apply(builder)

    fun meta(builder: ItemMeta.() -> Unit) = meta
        .apply(builder)
        .also { stack.itemMeta = it }

    var name
        get() = meta.displayName
        set(value) {
            meta { displayName = value }
        }

    var lore
        get() = meta.lore
        set(value) {
            meta { lore = value }
        }

    var amount
        get() = stack.amount
        set(value) {
            stack.amount = value
        }
}