package network.warzone.mars.utils.menu

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

data class Coords(
    var x: Int,
    var y: Int
)

fun toSlot(x: Int, y: Int) = x + (y * 9)
fun fromSlot(s: Int) = Pair(s % 9, s / 9)

fun gui(
    title: String,
    rows: Int,
    render: GUI.() -> Unit
) = GUI(title, rows, render).apply(render)

fun Player.open(gui: GUI) = openInventory(gui.inventory)

class GUI(
    val title: String,
    val rows: Int,
    val render: GUI.() -> Unit
) : Listener {
    init {
        Mars.registerEvents(this)
    }

    val slots = arrayOfNulls<Slot>(9 * rows)

    val inventory = Bukkit.createInventory(null, rows * 9, title)

    fun refresh() {
        inventory.clear()
        for (i in 0 until 9 * rows)
            slots[i] = null
        this.render()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onClick(e: InventoryClickEvent) = runBlocking {
        if (e.inventory != inventory) return@runBlocking
        e.isCancelled = true

        if (e.clickedInventory != inventory) return@runBlocking
        val player = e.whoClicked as? Player ?: return@runBlocking
        val slot = slots.getOrNull(e.slot) ?: return@runBlocking

        // Play a sound if the slot has a click handler
        if (slot.onclick != null) {
            slot.sound!!(player)
            slot.onclick!!(e, player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onClose(e: InventoryCloseEvent) {
        if (e.inventory != inventory) return
        if (inventory.viewers.size > 0) return
        HandlerList.unregisterAll(this)
    }

    inner class Slot {
        var item: Item? = null
        var onclick: (suspend InventoryClickEvent.(Player) -> Unit)? = null
        var sound: (suspend (Player) -> Unit)? = { it.playSound(it.location, Sound.ORB_PICKUP, .05f, 1f) }
    }

    fun slot(
        i: Int,
        builder: GUI.Slot.() -> Unit
    ) {
        val slot = Slot().apply(builder)
        inventory.setItem(i, slot.item?.stack)
        slots[i] = slot
    }

    fun slot(
        x: Int, y: Int,
        builder: GUI.Slot.() -> Unit
    ) = slot(toSlot(x, y), builder)

    fun slot(
        coords: Coords,
        builder: GUI.Slot.() -> Unit
    ) = slot(coords.x, coords.y, builder)

    fun fill(
        x1: Int, y1: Int, x2: Int, y2: Int,
        builder: GUI.Slot.() -> Unit
    ) {
        val dx = if (x1 < x2) x1..x2 else x2..x1
        val dy = if (y1 < y2) y1..y2 else y2..y1
        for (x in dx) for (y in dy) slot(x, y, builder)
    }

    fun fill(
        coords1: Coords,
        coords2: Coords,
        builder: GUI.Slot.() -> Unit
    ) = fill(coords1.x, coords1.y, coords2.x, coords2.y, builder)

    fun all(builder: GUI.Slot.() -> Unit) = fill(0, 0, 8, rows - 1, builder)

    fun Coords.up() {
        y = Math.floorMod((y - 1), rows)
    }

    fun Coords.down() {
        y = Math.floorMod((y + 1), rows)
    }

    fun Coords.left() {
        x = Math.floorMod((x - 1), 9)
    }

    fun Coords.right() {
        x = Math.floorMod((x + 1), 9)
    }
}
