package network.warzone.mars.utils

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.DyeColor

private val dyeChatMap = mapOf(
    DyeColor.BLACK to ChatColor.DARK_GRAY,
    DyeColor.BLUE to ChatColor.DARK_BLUE,
    DyeColor.BROWN to ChatColor.GOLD,
    DyeColor.CYAN to ChatColor.AQUA,
    DyeColor.GRAY to ChatColor.GRAY,
    DyeColor.GREEN to ChatColor.DARK_GREEN,
    DyeColor.LIGHT_BLUE to ChatColor.BLUE,
    DyeColor.LIME to ChatColor.GREEN,
    DyeColor.MAGENTA to ChatColor.LIGHT_PURPLE,
    DyeColor.ORANGE to ChatColor.GOLD
)
fun colorFromName(name: String) : NamedTextColor? = NamedTextColor.NAMES.value(name)
