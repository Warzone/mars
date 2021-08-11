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
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.translateAlternateColorCodes

val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(WarzonePGM.get())

fun Rank.asTextComponent(): TextComponent {
    val translatedPrefix = colorize(this.prefix ?: "${RED}None")

    val hover = text()
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
        .append(
            text("\n"),
            text("Click to edit", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)
        )

    return text("- ${this.name}")
        .clickEvent(ClickEvent.suggestCommand("/rank edit ${this.name}"))
        .hoverEvent(HoverEvent.showText(hover.build()))
}

fun Boolean.asTextComponent(): TextComponent {
    return if (this) {
        text("Yes", NamedTextColor.GREEN)
    } else {
        text("No", NamedTextColor.RED)
    }
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

fun colorize(text: String): String
        = translateAlternateColorCodes('&', text)