package network.warzone.mars.utils.menu

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun item(
    type: Material = Material.STAINED_GLASS,
    data: Short = 0,
    builder: Item.() -> Unit = {}
) = Item(type, data).apply(builder)

class Item(
    val type: Material,
    data: Short = 0
) {
    var stack = ItemStack(type, 1, data)
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

    // Add enchantment to the item
    fun enchant(enchantment: Enchantment, level: Int = 1) {
        stack.addUnsafeEnchantment(enchantment, level)
    }

    // Add item flags to the item
    fun flags(vararg itemFlags: ItemFlag) {
        meta {
            addItemFlags(*itemFlags)
        }
    }
}