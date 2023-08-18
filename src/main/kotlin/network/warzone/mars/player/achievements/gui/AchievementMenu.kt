package network.warzone.mars.player.achievements.gui

import network.warzone.api.database.models.Achievement
import network.warzone.mars.player.achievements.AchievementDebugger
import network.warzone.mars.player.achievements.AchievementManager.filterAchievementsWithParent
import network.warzone.mars.player.achievements.AchievementManager.getAchievementsForCategory
import network.warzone.mars.player.achievements.AchievementManager.getParentsFromAchievements
import network.warzone.mars.player.achievements.models.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.utils.menu.*
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemFlag
import kotlin.math.min

class AchievementMenu(private val player: Player) : Listener, AchievementDebugger {
    private val profile: PlayerProfile = PlayerFeature.getCached(player.name)!!

    fun openMainMenu(): GUI {
        return gui("§6§lAchievements", 1) {
            val categories = listOf("Kills", "Losses", "Wins", "Deaths", "Objectives", "Misc")
            for ((index, category) in categories.withIndex()) {
                slot(index) {
                    item = item(Material.BOOKSHELF) {
                        name = "§b$category"
                        lore = wrap("§7Click to view this category.", 40)
                    }
                    onclick = { player ->
                        player.open(openCategory(category))
                    }
                }
            }
        }
    }

    fun openCategory(categoryName: String, page: Int = 1): GUI {
        return gui("§b§l$categoryName", 6) {
            val achievements = getAchievementsForCategory(categoryName)
            val parents = getParentsFromAchievements(achievements)

            val (nonEdgeStart, nonEdgeEnd) = calculateNonEdgeIndices(parents.size, page)

            for ((index, parent) in parents.subList(nonEdgeStart, nonEdgeEnd).withIndex()) {
                slot(getSlotFromNonEdgeIndex(index)) {
                    item = item(Material.BOOK) {
                        name = "§a${parent.displayName}"
                        lore = wrap("§7${parent.description}", 40)
                    }

                    onclick = { player ->
                        player.open(openAchievementDetails(parent, achievements))
                    }
                }
            }
            // Previous Page Arrow
            if (page > 1) {
                slot(45) {
                    createPreviousPageArrow(page) { player ->
                        player.open(openCategory(categoryName, page - 1))
                    }()
                }
            }
            // Next Page Arrow
            if ((parents.size - 1) / 36 + 1 > page) {
                slot(53) {
                    createNextPageArrow(page) { player ->
                        player.open(openCategory(categoryName, page + 1))
                    }()
                }
            }
            // Back Arrow
            slot(49) {
                createBackArrow { player ->
                    player.open(openMainMenu())
                }()
            }
            slot(4) {
                item = item(Material.BOOKSHELF) {
                    name = "§b$categoryName"
                    lore = wrap("§7This menu lists achievements of the §b${categoryName} §7category type.", 30)
                }
            }
        }
    }

    fun openAchievementDetails(parent: AchievementParent, achievements: List<Achievement>, page: Int = 1): GUI {
        return gui("§a§l${parent.displayName}", 6) {
            val matchingAchievements = filterAchievementsWithParent(parent, achievements)

            val (nonEdgeStart, nonEdgeEnd) = calculateNonEdgeIndices(matchingAchievements.size, page)

            for ((index, achievement) in matchingAchievements.subList(nonEdgeStart, nonEdgeEnd).withIndex()) {
                slot(getSlotFromNonEdgeIndex(index)) {
                    item = item(Material.PAPER) {
                        name = "§d${achievement.name}"
                        lore = wrap("§7${achievement.description}", 40)

                        // Check if the player has the achievement
                        if (profile.stats.achievements.contains(achievement.name)) {
                            enchant(Enchantment.DURABILITY)
                            flags(ItemFlag.HIDE_ENCHANTS)
                        }
                    }
                }
            }
            // Previous Page Arrow
            if (page > 1) {
                slot(45) {
                    createPreviousPageArrow(page) { player ->
                        player.open(openAchievementDetails(parent, achievements, page - 1))
                    }()
                }
            }
            // Next Page Arrow
            if ((matchingAchievements.size - 1) / 36 + 1 > page) {
                slot(53) {
                    createNextPageArrow(page) { player ->
                        player.open(openAchievementDetails(parent, achievements, page + 1))
                    }()
                }
            }
            // Back Arrow
            slot(49) {
                createBackArrow { player ->
                    player.open(openCategory(parent.category, 1))
                }()
            }
            slot(4) {
                item = item(Material.BOOK) {
                    name = "§a${parent.displayName}"
                    lore = wrap("§7This menu lists milestones for the current achievement.", 40)
                }
            }
        }
    }

    private fun createBackArrow(action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "§7Back"
            }
            onclick = action
        }
    }

    private fun createNextPageArrow(currentPage: Int, action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "§7Next Page"
                amount = currentPage + 1
            }
            onclick = action
        }
    }

    private fun createPreviousPageArrow(currentPage: Int, action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "§7Previous Page"
                amount = currentPage - 1
            }
            onclick = action
        }
    }

    /**
     * Get non-edge slot indices for achievements based on the current page.
     */
    private fun calculateNonEdgeIndices(totalSize: Int, page: Int): Pair<Int, Int> {
        val nonEdgeSlotsPerPage = 4 * 7 // 4 rows * 7 columns
        val startIndex = (page - 1) * nonEdgeSlotsPerPage
        val endIndex = min(startIndex + nonEdgeSlotsPerPage, totalSize)
        return startIndex to endIndex
    }

    /**
     * Calculate the GUI slot position based on non-edge index.
     */
    private fun getSlotFromNonEdgeIndex(index: Int): Int {
        val row = index / 7 + 1 // 1 is added to skip the first row
        val col = index % 7 + 1 // 1 is added to skip the first column
        return row * 9 + col // 9 columns in a row
    }
}