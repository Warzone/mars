package network.warzone.pgm.utils

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.ranks.models.Rank
import org.bukkit.ChatColor.*

val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(WarzonePGM.get())

fun Rank.asTextComponent(): TextComponent {
    val translatedPrefix = colorize(this.prefix ?: "${RED}None")

    val hoverComponent = literalText {
        text(this@asTextComponent.displayName ?: this@asTextComponent.name) {
            color = NamedTextColor.GOLD
            underline = true
        }
        emptyLine()
        text("Name: ") {
            color = NamedTextColor.GRAY
            text(this@asTextComponent.name) {
                color = NamedTextColor.YELLOW
            }
        }
        newLine()
        text("Prefix: ") {
            color = NamedTextColor.GRAY
            text(translatedPrefix)
        }
        newLine()
        text("Priority: ") {
            color = NamedTextColor.GRAY
            text(this@asTextComponent.priority.toString()) {
                color = NamedTextColor.YELLOW
            }
        }
        newLine()
        text("Staff: ") {
            color = NamedTextColor.GRAY
            text(this@asTextComponent.staff.asTextComponent())
        }
        newLine()
        text("Default: ") {
            color = NamedTextColor.GRAY
            text(this@asTextComponent.applyOnJoin.asTextComponent())
        }
        newLine()
        text("Permissions: ") {
            color = NamedTextColor.GRAY
            text(this@asTextComponent.permissions.size.toString()) {
                color = NamedTextColor.WHITE
                bold = true
            }
        }
        emptyLine()
        text("Click to edit") {
            color = NamedTextColor.LIGHT_PURPLE
            italic = true
        }
    }

    return literalText("- ${this.name}") {
        onClickCommand("/rank edit ${this@asTextComponent.name}", onlySuggest = true)
        hoverText(hoverComponent)
    }

}

fun Boolean.asTextComponent(): TextComponent {
    return if (this) {
        literalText("Yes") { color = NamedTextColor.GREEN }
    } else {
        literalText("No") { color = NamedTextColor.RED }
    }
}

fun colorize(text: String): String
        = translateAlternateColorCodes('&', text)