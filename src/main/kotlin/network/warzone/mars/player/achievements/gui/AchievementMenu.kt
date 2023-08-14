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
        return gui("Achievements", 1) {
            val categories = listOf("Kills", "Losses", "Wins", "Deaths", "Objectives", "Misc")
            for ((index, category) in categories.withIndex()) {
                slot(index) {
                    item = item(Material.BOOKSHELF) {
                        name = category
                    }
                    onclick = { player ->
                        player.open(openCategory(category))
                    }
                }
            }
        }
    }

    fun openCategory(categoryName: String, page: Int = 1): GUI {
        return gui(categoryName, 6) {
            val achievements = getAchievementsForCategory(categoryName)
            val parents = getParentsFromAchievements(achievements)

            // Determine start and end indices for paging
            val startIndex = (page - 1) * 36
            val endIndex = min(startIndex + 36, parents.size)

            for ((index, parent) in parents.subList(startIndex, endIndex).withIndex()) {
                slot(index) {
                    item = item(Material.BOOK) {
                        name = parent.displayName
                        lore = wrap(parent.description)
                    }

                    onclick = { player ->
                        player.open(openAchievementDetails(parent, achievements))
                    }
                }
            }
            // Previous Page Arrow
            if (page > 1) {
                slot(45) {
                    createPreviousPageArrow { player ->
                        player.open(openCategory(categoryName, page - 1))
                    }()
                }
            }
            // Next Page Arrow
            if ((parents.size - 1) / 36 + 1 > page) {
                slot(53) {
                    createNextPageArrow { player ->
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
        }
    }

    fun openAchievementDetails(parent: AchievementParent, achievements: List<Achievement>, page: Int = 1): GUI {
        return gui(parent.displayName, 6) {
            val matchingAchievements = filterAchievementsWithParent(parent, achievements)

            // Determine start and end indices for paging
            val startIndex = (page - 1) * 36
            val endIndex = min(startIndex + 36, matchingAchievements.size)

            for ((index, achievement) in matchingAchievements.subList(startIndex, endIndex).withIndex()) {
                slot(index) {
                    item = item(Material.PAPER) {
                        name = achievement.name
                        lore = wrap(achievement.description)

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
                    createPreviousPageArrow { player ->
                        player.open(openAchievementDetails(parent, achievements, page - 1))
                    }()
                }
            }
            // Next Page Arrow
            if ((matchingAchievements.size - 1) / 36 + 1 > page) {
                slot(53) {
                    createNextPageArrow { player ->
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
        }
    }

    private fun createBackArrow(action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "Back"
            }
            onclick = action
        }
    }

    private fun createNextPageArrow(action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "Next Page"
            }
            onclick = action
        }
    }

    private fun createPreviousPageArrow(action: suspend InventoryClickEvent.(Player) -> Unit): GUI.Slot.() -> Unit {
        return {
            item = item(Material.ARROW) {
                name = "Previous Page"
            }
            onclick = action
        }
    }
}