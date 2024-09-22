package network.warzone.mars.utils.menu

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.sound
import network.warzone.mars.Mars
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import network.warzone.mars.utils.Sounds
import org.bukkit.Bukkit
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

fun Player.open(gui: GUI) = Mars.sync{ openInventory(gui.inventory) }

class GUI(
    val title: String,
    val rows: Int,
    val render: GUI.() -> Unit
) : Listener {

    companion object {
        val ORB_PICKUP: Sound = sound(Sounds.RANDOM_ORB, Sound.Source.MASTER, 0.05f, 1f)
    }

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
    fun onClick(e: InventoryClickEvent) {
        if (e.inventory != inventory) return
        e.isCancelled = true

        if (e.clickedInventory != inventory) return
        val player = e.whoClicked as? Player ?: return
        val slot = slots.getOrNull(e.slot) ?: return

        // Play a sound if the slot has a click handler
        if (slot.onclick != null) {
            slot.sound!!(e, player)
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
        var onclick: (InventoryClickEvent.(Player) -> Unit)? = null
        var sound: (InventoryClickEvent.(Player) -> Unit)? = { AUDIENCE_PROVIDER.player(it).playSound(ORB_PICKUP) }
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
