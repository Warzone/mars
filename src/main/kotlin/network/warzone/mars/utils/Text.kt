package network.warzone.mars.utils

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.time4j.ClockUnit
import network.warzone.mars.Mars
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.translateAlternateColorCodes
import java.time.Duration
import java.util.*


val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(Mars.get())

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

    if (editable) hover =
        hover.append { text("\n") }.append { text("Click to edit", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC) }

    var finalComponent = if (this.prefix != null) {
        text()
            .append { text(this.prefix!!.color()) }
            .append { text(" (", NamedTextColor.GRAY) }
            .append { text(this.name, NamedTextColor.GRAY) }
            .append(text(")", NamedTextColor.GRAY))
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

fun Date.getTimeAgo(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val currentCalendar = Calendar.getInstance()

    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
    val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentCalendar.get(Calendar.MINUTE)

    return if (year < currentYear) {
        val interval = currentYear - year
        if (interval == 1) "$interval year ago" else "$interval years ago"
    } else if (month < currentMonth) {
        val interval = currentMonth - month
        if (interval == 1) "$interval month ago" else "$interval months ago"
    } else if (day < currentDay) {
        val interval = currentDay - day
        if (interval == 1) "$interval day ago" else "$interval days ago"
    } else if (hour < currentHour) {
        val interval = currentHour - hour
        if (interval == 1) "$interval hour ago" else "$interval hours ago"
    } else if (minute < currentMinute) {
        val interval = currentMinute - minute
        if (interval == 1) "$interval minute ago" else "$interval minutes ago"
    } else {
        "a moment ago"
    }
}

// only displays days and hours ("7d 3h") - used for punishment lengths
fun Duration.format(): String {
    val dur = net.time4j.Duration.of(this.seconds, ClockUnit.SECONDS)
    val interim = net.time4j.Duration.formatter("[##D]'d' [##h]'h'").format(dur.toTemporalAmount()).split(" ")
    val formatted = mutableListOf<String>()
    interim.forEach { if (it.length > 1) formatted.add(it) }
    return formatted.joinToString("")
}

// only displays hours, minutes and seconds ("7h 2m 23s") - used for playtime
fun Duration.conciseFormat(): String {
    return this.toString()
        .substring(2)
        .replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
        .replace("\\.\\d+".toRegex(), "")
        .toLowerCase()
}

fun String.chunkedWords(size: Int): List<String> {
    val words = this.split(' ')
    val chunked = words.chunked(size)
    return chunked.map { it.joinToString(" ") }
}