package network.warzone.pgm.utils

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.tags.models.Tag
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.translateAlternateColorCodes

val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(WarzonePGM.get())

fun Rank.asTextComponent(editable: Boolean = true): TextComponent {
    val translatedPrefix = (this.prefix ?: "${RED}None").color()

    var hover = text()
        .append(
            text(this.name, NamedTextColor.GOLD, TextDecoration.UNDERLINED),
            text("\n\n")
        )
        .append { createStandardLabelled("Display Name", this.displayName ?: "None") }
        .append { createUncolouredLabelled("Prefix", translatedPrefix) }
        .append { createNumberedLabelled("Priority", this.priority) }
        .append { createBooleanLabelled("Staff", this.staff) }
        .append { createBooleanLabelled("Default", this.applyOnJoin) }
        .append { createNumberedLabelled("Permissions", this.permissions.size) }

    if (editable) hover = hover.append{ text("\n") }.append { text("Click to edit", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC) }

    var finalComponent = if (this.prefix != null) {
        text()
            .append { text(this.prefix!!.color()) }
            .append { text(" (", NamedTextColor.GRAY) }
            .append { text(this.name, NamedTextColor.GRAY) }
            .append( text(")", NamedTextColor.GRAY) )
    } else {
        text(this.name, NamedTextColor.GRAY).toBuilder()
    }

    finalComponent = finalComponent.hoverEvent(HoverEvent.showText(hover.build()))

    if (editable) finalComponent = finalComponent.clickEvent(ClickEvent.suggestCommand("/rank edit ${this.name}"))

    return finalComponent.build()
}

fun Tag.asTextComponent(complex: Boolean = false): TextComponent {
    return if (complex) {
        text()
            .append { text(this.display.color()) }
            .append { text(" (", NamedTextColor.GRAY) }
            .append { text(this.name, NamedTextColor.GRAY) }
            .append { text(")", NamedTextColor.GRAY) }
            .build()
    } else {
        text(this.display.color())
    }
}

fun Boolean.asTextComponent(): TextComponent {
    return if (this) {
        text("Yes", NamedTextColor.GREEN)
    } else {
        text("No", NamedTextColor.RED)
    }
}

/**
 *        val differenceDisplay: String = difference.joinToString("\n") {
"$YELLOW${it.name} $DARK_GRAY:: $WHITE$BOLD${it.source} $RESET$GRAY-> $WHITE$BOLD${it.new}"
}
 */
fun Difference.asTextComponent(): TextComponent {
    return text()
        .append { text(this.name, NamedTextColor.YELLOW) }
        .append { text(" :: ", NamedTextColor.DARK_GRAY) }
        .append { text(this.source, NamedTextColor.WHITE, TextDecoration.BOLD) }
        .append { text(" -> ", NamedTextColor.GRAY) }
        .append { text(this.new, NamedTextColor.WHITE, TextDecoration.BOLD) }
        .build()
}

fun createStandardLabelled(label: String, value: String): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value, NamedTextColor.YELLOW),
        text("\n")
    )
}

fun createNumberedLabelled(label: String, value: Int): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value.toString(), NamedTextColor.WHITE, TextDecoration.BOLD),
        text("\n")
    )
}

fun createBooleanLabelled(label: String, value: Boolean): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        value.asTextComponent(),
        text("\n")
    )
}

fun createUncolouredLabelled(label: String, value: String): Component {
    return Component.join(
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value),
        text("\n")
    )
}

fun String.color(): String {
    return translateAlternateColorCodes('&', this)
}