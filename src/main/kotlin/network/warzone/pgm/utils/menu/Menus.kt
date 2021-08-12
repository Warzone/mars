package network.warzone.pgm.utils.menu

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

typealias MenuAction = suspend (ActionInfo) -> Unit
typealias Slot = Pair<Int, Int>

fun Slot.raw(): Int {
    return (this.first - 1) * 9 + (this.second - 1)
}

fun slotOf(raw: Int): Slot {
    return Slot(raw / 9, raw % 9)
}

data class ActionInfo(val player: Player, val event: InventoryClickEvent)

abstract class Menu(val name: String, private val rows: Int) : Listener {

    internal val inventory: Inventory = Bukkit.createInventory(null, rows * 9, name)
    private val actions: MutableMap<Int, MenuAction> = mutableMapOf()

    init {
        finishInit()
    }

    open suspend fun draw() {}

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun set(slot: Slot, item: ItemStack, action: MenuAction?) {
        inventory.setItem(slot.raw(), item)

        action?.let {
            actions[slot.raw()] = action
        }
    }

    fun setRow(row: Int, item: ItemStack, action: MenuAction?) {
        for (column in 1..9) {
            val slot = Slot(row, column)

            set(slot, item, action)
        }
    }

    fun setColumn(column: Int, item: ItemStack, action: MenuAction?) {
        for (row in 1..rows) {
            val slot = Slot(row, column)

            set(slot, item, action)
        }
    }

    fun clear() {
        inventory.clear()
        actions.clear()
    }

    internal fun disable() {
        HandlerList.unregisterAll(this)
    }

    private fun finishInit() {
        WarzonePGM.registerEvents(this)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) = runBlocking {
        if (event.inventory.equals(inventory)) {
            event.isCancelled = true

            val player = event.whoClicked as? Player ?: return@runBlocking

            if (actions.contains(event.slot)) {
                actions[event.slot]?.invoke(ActionInfo(player, event))
            }
        }
    }

}