package network.warzone.mars.player.achievements

import network.warzone.mars.Mars
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.achievements.AchievementParentAgent
import network.warzone.mars.player.feature.PlayerFeature
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

class AchievementMenu(private val player: Player) : Listener {
    private val mainMenu: Inventory = Bukkit.createInventory(null, 9, "Achievements")
    private val subMenus: MutableMap<AchievementCategory, Inventory> = AchievementCategory.values().associateBy({ it }, { Bukkit.createInventory(null, 54, it.displayName) }).toMutableMap()
    private val individualMenus: MutableMap<String, Inventory> = AchievementManager.achievementParentAgents.keys.associateBy({ it.parentName }, { Bukkit.createInventory(player, 54, it.parentName) }).toMutableMap()
    private val previousInventory: Queue<Inventory> = LinkedList()

    init {
        Mars.registerEvents(this)
        createMainMenu()
        createSubMenus()
        createIndividualMenus()
    }

    /**
     * Private helper method for creating the ItemStack of the back arrow.
     */
    private fun createBackArrow(): ItemStack {
        val item = ItemStack(Material.ARROW)
        val meta = item.itemMeta
        meta?.displayName = "Back"
        item.itemMeta = meta
        return item
    }

    /**
     * Private helper method returning true/false if the back arrow is clicked.
     *
     * (This might be a redundant function...)
     */
    private fun backArrowClicked(event: InventoryClickEvent) : Boolean {
        if (event.currentItem?.itemMeta?.displayName == "Back" && event.currentItem?.type == Material.ARROW) {
            return true;
        }
        return false;
    }

    /**
     * Creates the display portion of the main menu. Functionality is
     * handled in the onClick event.
     */
    private fun createMainMenu() {
        AchievementCategory.map.forEach {
            val item = ItemStack(Material.BOOKSHELF)
            val meta = item.itemMeta
            meta?.displayName = it.key
            item.itemMeta = meta
            mainMenu.addItem(item)
        }

    }

    /**
     * Creates the display portion of the sub menus. A sub menu is
     * created for each of the six achievement categories. The
     * AchievementParents are placed in these corresponding menus.
     *
     * Again, functionality is handled in the onClick event.
     */
    private fun createSubMenus() {
        AchievementManager.achievementParentAgents.keys.forEach {
            println("    it.name: " + it.parentName)
            val inv: Inventory? = subMenus[it.category]
            val item = ItemStack(Material.BOOK)
            val meta = item.itemMeta
            meta?.displayName = it.parentName
            meta?.lore = listOf(it.description)
            item.itemMeta = meta
            inv?.addItem(item)
            inv?.setItem(inv.size - 5, createBackArrow())
        }
        AchievementCategory.values().forEach {
            val inv: Inventory? = subMenus[it]
            inv?.setItem(inv.size - 5, createBackArrow())
        }
    }

    /**
     * Creates the display portion of the individual menus. An
     * individual menu is created for each AchievementParent enum,
     * and all Achievements are placed in an individual menu based
     * on the corresponding parent defined within them.
     *
     * Functionality is in the onClick event.
     */
    private fun createIndividualMenus() {
        AchievementManager.achievementParentAgents.values.flatten().forEach { achievementAgent ->
            val inv: Inventory? = individualMenus[achievementAgent.parent.agent.parentName]
            val item = ItemStack(Material.PAPER)
            val meta = item.itemMeta
            PlayerFeature.getCached(player.uniqueId)?.let { profile ->
                meta?.displayName = achievementAgent.title
                meta?.lore = listOf(achievementAgent.description)
                if (profile.stats.achievements.contains(achievementAgent.id)) {
                    // Add hidden enchantment if the player has obtained this achievement.
                    meta?.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
                    meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                }
                item.itemMeta = meta
            }
            inv?.addItem(item)
            inv?.setItem(inv.size - 5, createBackArrow())
        }
    }

    //


    /**
     * Opens the main menu.
     *
     * If current remains null, then openMainMenu is being called from /achievements.
     * Otherwise, it's being called while a subMenu is open (back arrow into main menu)
     */
    fun openMainMenu(current: Inventory? = null) {
        previousInventory.add(current ?: mainMenu)
        player.openInventory(mainMenu)
    }

    /**
     * Opens a sub menu.
     *
     * If category remains null, then openSubMenu is being called while the current menu
     * is an individualMenu (back arrow into sub menu). Otherwise, it's being called while
     * mainMenu is the current inventory.
     */
    fun openSubMenu(current: Inventory, category: AchievementCategory? = null) {
        val inv: Inventory = subMenus[category] ?: previousInventory.peek()
        previousInventory.add(current)
        player.openInventory(inv)
    }

    /**
     * Opens an individual menu.
     */
    fun openIndividualMenu(current: Inventory, parentName: String) {
        individualMenus[parentName]?.let {
            previousInventory.add(current)
            player.openInventory(it)
        }
    }

    /**
     * Handles the click functionality for all menus.
     */
    @EventHandler(priority = EventPriority.LOW)
    fun onClick(event: InventoryClickEvent) {

        /** Ensures a player triggered the event, and sets up some variables */
        if (event.whoClicked != player) return
        event.isCancelled = true
        val clickedInventory = event.clickedInventory ?: return

        /** The currently opened inventory is mainMenu; someone has clicked inside of it */
        if (clickedInventory == mainMenu) {
            val item = event.currentItem ?: return
            val itemMeta = item.itemMeta ?: return
            val category = ChatColor.stripColor(itemMeta.displayName) ?: return
            AchievementCategory.fromString(category)?.let { openSubMenu(clickedInventory, it) }
            return;
        }

        /** The currently opened inventory is a subMenu */
        else if (subMenus.values.contains(clickedInventory)) {
            if (backArrowClicked(event)) { openMainMenu(clickedInventory); return }
            val item = event.currentItem ?: return
            val itemMeta = item.itemMeta ?: return
            val parentType = ChatColor.stripColor(itemMeta.displayName) ?: return
            AchievementParent.fromString(parentType)?.let { openIndividualMenu(clickedInventory, it.parentName) }
            return;
        }

        /** The currently opened inventory is an individualMenu */
        else if (individualMenus.values.contains(clickedInventory)) {
            if (backArrowClicked(event)) { openSubMenu(clickedInventory); return }
            return;
        }

        /** No relevant inventory is currently opened */
        else {
            return;
        }
    }

    /**
     * Updates the previousInventory variable and ensures the onClick event is
     * unregistered at the appropriate time.
     *
     * Note: player.openInventory(<inv>) triggers this event if the function is
     * called while the player already has an inventory open.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.player == player && previousInventory.isNotEmpty()) {
            previousInventory.poll()
            if (previousInventory.isEmpty()) {
                println("EVENTS ARE BEING UNREGISTERED")
                HandlerList.unregisterAll(this)
            }
        }
    }
}