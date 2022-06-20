package network.warzone.mars.utils

import net.time4j.ClockUnit
import network.warzone.mars.Mars
import network.warzone.mars.player.feature.LevelColorService
import network.warzone.mars.player.feature.PlayerService
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.commands.PunishCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentKind
import network.warzone.mars.punishment.models.StaffNote
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import org.bukkit.ChatColor
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.translateAlternateColorCodes
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.time.Duration
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

val AUDIENCE_PROVIDER: BukkitAudiences = BukkitAudiences.create(Mars.get())

val JOIN_CONFIG = JoinConfiguration.noSeparators()

fun Rank.asTextComponent(editable: Boolean = true): TextComponent {
    val translatedPrefix = (this.prefix ?: "${RED}None").color()

    var hover = text()
        .append(
            text(this.name, NamedTextColor.GOLD, TextDecoration.UNDERLINED),
            text("\n\n")
        )
        .append { createStandardLabelled("Display Name", this.displayName ?: "None") }
        .append { createUncoloredLabelled("Prefix", translatedPrefix) }
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

fun Punishment.asHoverComponent(revertable: Boolean = true): TextComponent {
    var hover = text()
        .append(
            text("${this.action.kind.color}${this.reason.name} (${this.offence}) – ${this.action.kind.noun} (${this.action.formatLength()})"),
            newline(),
            newline()
        )
        .append { createUncoloredLabelled("Issued by", this.punisher?.name ?: "CONSOLE") }
        .append { createUncoloredLabelled("Issued at", "${this.issuedAt} (${this.issuedAt.getRelativeTime()})") }
        .append { createUncoloredLabelled("Expires", if (action.isPermanent()) "Never" else "${expiresAt.toString()} (${this.expiresAt.getRelativeTime()})") }
//        .append { createNumberedLabelled("Known IPs", this.targetIps.size) }

    if (this.note != null) hover = hover.append { createStandardLabelled("Note", this.note) }

    hover = hover
        .append { createBooleanLabelled("Silent", this.silent) }
        .append { createBooleanLabelled("Active", this.isActive) }
        .append { createBooleanLabelled("Reverted", this.reversion != null) }

    if (this.reversion != null) hover = hover.append { newline() }
        .append { createUncoloredLabelled("Reverted by", this.reversion.reverter.name) }
        .append {
            createUncoloredLabelled(
                "Reverted at",
                "${Date(this.reversion.revertedAt)} (${Date(this.reversion.revertedAt).getRelativeTime()})"
            )
        }
        .append { createUncoloredLabelled("Reversion reason", this.reversion.reason) }

    if (revertable && this.reversion == null) hover.append(newline(), text("Click to revert", NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC))

    return hover.build()
}

fun Punishment.asTextComponent(revertable: Boolean = true): TextComponent {

    var finalComponent =
        text("[", NamedTextColor.GRAY)
            .append(text(this.issuedAt.getRelativeTime().toUpperCase(), NamedTextColor.GRAY))
            .append(text("]", NamedTextColor.GRAY))
            .append(space())
            .append(text("${if (this.isActive) this.action.kind.color else ChatColor.GRAY}${if (this.isReverted) ChatColor.STRIKETHROUGH else ""}${this.action.kind.verb.toUpperCase()}"))
            .append(space())
            .append(text(this.target.name, NamedTextColor.WHITE))

    finalComponent = finalComponent.hoverEvent(HoverEvent.showText(this.asHoverComponent(revertable)))

    if (revertable && this.reversion == null) finalComponent = finalComponent.clickEvent(ClickEvent.runCommand("/revertp ${this._id}"))

    return finalComponent
}

fun PlayerService.PlayerAltResponse.asTextComponent(): TextComponent {
    val isMuted = this.punishments.any { it.action.kind == PunishmentKind.MUTE && it.isActive }
    val isBanned = this.punishments.any { it.action.isBan() && it.isActive }
    val color = if (isBanned) NamedTextColor.RED else if (isMuted) NamedTextColor.YELLOW else NamedTextColor.GRAY

    val hover = PunishCommands.createPlayerLore(player, this.punishments)
    val component = text(this.player.name, color).hoverEvent(
        HoverEvent.showText(
            PlainTextComponentSerializer.plainText().deserialize(hover.joinToString("\n"))
        )
    )
    return component
}

fun StaffNote.asTextComponent(player: String, deletable: Boolean): TextComponent {
    var hover = text()
        .append { createNumberedLabelled("ID", this.id) }
        .append { createUncoloredLabelled("Added at", "${this.createdAt} (${this.createdAt.getRelativeTime()})") }
        .append { createStandardLabelled("Author", this.author.name) }

    if (deletable) hover = hover.append { newline() }.append { text("Click to delete", NamedTextColor.LIGHT_PURPLE) }

    var finalComponent = text("${this.id}.", NamedTextColor.AQUA)
        .append { space() }
        .append { text(this.content, NamedTextColor.YELLOW) }
        .append { space() }
        .append { text("(${this.author.name})", NamedTextColor.GRAY) }
        .hoverEvent(HoverEvent.showText(hover.build()))

    if (deletable) finalComponent = finalComponent.clickEvent(ClickEvent.suggestCommand("/notes $player del $id"))

    return finalComponent
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
    return join(
        JOIN_CONFIG,
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value, NamedTextColor.YELLOW),
        text("\n")
    )
}

fun createNumberedLabelled(label: String, value: Int): Component {
    return join(
        JOIN_CONFIG,
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value.toString(), NamedTextColor.WHITE, TextDecoration.BOLD),
        text("\n")
    )
}

fun createBooleanLabelled(label: String, value: Boolean): Component {
    return join(
        JOIN_CONFIG,
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        value.asTextComponent(),
        text("\n")
    )
}

fun createUncoloredLabelled(label: String, value: String): Component {
    return join(
        JOIN_CONFIG,
        text(""),
        text("$label: ", NamedTextColor.GRAY),
        text(value),
        text("\n")
    )
}

fun String.color(): String {
    return translateAlternateColorCodes('&', this)
}

@Deprecated(
    message = "Replaced with a general time span formatting method.",
    replaceWith = ReplaceWith("getRelativeTime")
)
fun Date.getTimeAgo(): String {
    return this.getRelativeTime()
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

fun getLevelAsComponent(level: Int): Component {
    return text(
        "[$level]",
        LevelColorService.chatColorFromLevel(level)
    )
}

fun getPlayerLevelAsComponent(profile: PlayerProfile): Component {
    return getLevelAsComponent(profile.stats.level)
}

/**
 * Compares two dates (current time if comparison date is set to null) and uses the difference in seconds to return a
 * formatted time span using the [formatTimeSpan] method. Will be prefixed by "in" if the comparison date is future in
 * relation to the date it is being checked upon; will be suffixed by "ago" if the comparison date is past in the same
 * relation; or will return "right now" if both dates are equal at a precision level of seconds.
 *
 * Examples:
 * <b>Input:</b> 7245 (seconds)
 * <b>Output (precise): "2 hours 45 seconds ago"</b>
 * <b>Output (non-precise): "2 hours ago"</b>
 *
 * <b>Input:</b> -5085 (seconds)
 * <b>Output (precise): in 1 hour 24 minutes 45 seconds</b>
 * <b>Output (non-precise): in 1 hour</b>
 *
 * @param s Time in seconds
 * @param precise boolean control for the precision
 *
 * @return A formatted time span relative to the comparison date.
 */
fun Date.getRelativeTime(other: Date? = null, precise: Boolean = false): String {
    val o = other ?: Date()
    val difference = (o.time - this.time) / 1000 // Second-level precision
    return if (difference < 0) { // other is future
        "in " + formatTimeSpan(abs(difference), precise)
    } else if (difference > 0) { // other is past
        formatTimeSpan(abs(difference), precise) + " ago"
    } else {
        "right now"
    }
}

/**
 * Takes an amount of time in seconds and outputs a formatted time span. A precise formatted time span contains every
 * unit of time (years, months, days, hours, minutes and seconds) which are greater than or equal to one, as opposed to
 * non-precise formatted time spans which only contain the single highest unit that is greater than or equals to one.
 *
 * Examples:
 * <b>Input:</b> 7245 (seconds)
 * <b>Output (precise): 2 hours 45 seconds</b>
 * <b>Output (non-precise): 2 hours</b>
 *
 * @param s time in seconds
 * @param precise boolean control for the precision
 *
 * @return A formatted time span.
 */
fun formatTimeSpan(s: Long, precise: Boolean = false): String {
    var years   = floor(    s / 31_536_000.0).toInt()
    var months  = floor((   s % 31_536_000.0) / 2_592_000).toInt()
    var days    = floor(((  s % 31_536_000.0) % 2_592_000) / 86_400).toInt()
    var hours   = floor(((( s % 31_536_000.0) % 2_592_000) % 86_400) / 3600).toInt()
    var minutes = floor(((((s % 31_536_000.0) % 2_592_000) % 86_400) % 3600) / 60).toInt()
    var seconds = floor(((((s % 31_536_000.0) % 2_592_000) % 86_400) % 3600) % 60).toInt()

    var stringJoiner = StringJoiner(" ")
    if (years > 0) {
        stringJoiner.add("$years year${if (years == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    if (months > 0) {
        stringJoiner.add("$months month${if (months == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    if (days > 0) {
        stringJoiner.add("$days day${if (days == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    if (hours > 0) {
        stringJoiner.add("$hours hour${if (hours == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    if (minutes > 0) {
        stringJoiner.add("$minutes minute${if (minutes == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    if (seconds > 0) {
        stringJoiner.add("$seconds second${if (seconds == 1) "" else "s"}")
        if (!precise) return stringJoiner.toString()
    }
    return stringJoiner.toString().ifBlank { "0 seconds" }
}