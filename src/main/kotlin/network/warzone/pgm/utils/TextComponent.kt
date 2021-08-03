package network.warzone.pgm.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Opens a [LiteralTextBuilder].
 *
 * @param baseText the text you want to begin with, it is okay to let this empty
 * @param builder the builder which can be used to set the style and add child text components
 */
inline fun literalText(
    baseText: String = "",
    builder: LiteralTextBuilder.() -> Unit = { }
) = LiteralTextBuilder(baseText).apply(builder).build()

class LiteralTextBuilder(val internalText: TextComponent) {
    constructor(text: String) : this(Component.text(text))

    var bold: Boolean? = null
    var italic: Boolean? = null
    var underline: Boolean? = null
    var strikethrough: Boolean? = null
    var obfuscate: Boolean? = null

    /**
     * The text color.
     * This can be set in the following way:
     *
     * e.g. Medium turquoise:
     *  - `color = col(0x4BD6CB)`
     *  - `color = col(4970187)`
     *  - `color = col("#4BD6CB")`
     *  - `color = KColors.MEDIUMTURQUOISE`
     */
    var color: TextColor? = null

    var clickEvent: ClickEvent? = null
    var hoverEvent: HoverEvent<*>? = null

    val siblingText = Component.text("")

    /**
     * Append text to the parent.
     *
     * @param text the raw text (without formatting)
     * @param builder the builder which can be used to set the style and add child text components
     */
    inline fun text(
        text: String = "",
        builder: LiteralTextBuilder.() -> Unit = { }
    ) {
        siblingText.append(LiteralTextBuilder(text).apply(builder).build())
    }

    /**
     * Append text to the parent.
     *
     * @param text the text instance
     * @param builder the builder which can be used to set the style and add child text components
     */
    inline fun text(
        text: TextComponent,
        builder: LiteralTextBuilder.() -> Unit = { }
    ) {
        siblingText.append(LiteralTextBuilder(text).apply(builder).build())
    }

    /**
     * Sets the text which should be displayed when hovering
     * over the text in the chat.
     *
     * @param text the raw text (without formatting)
     * @param builder the builder which can be used to set the style and add child text components
     */
    inline fun hoverText(
        text: String = "",
        builder: LiteralTextBuilder.() -> Unit = { }
    ) {
        hoverEvent = HoverEvent.hoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            Component.join(Component.text(""), arrayListOf(LiteralTextBuilder(text).apply(builder).build()))
        )
    }

    inline fun hoverText(
        text: TextComponent,
        builder: LiteralTextBuilder.() -> Unit = { }
    ) {
        hoverEvent = HoverEvent.hoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            LiteralTextBuilder(text).apply(builder).build()
        )
    }

    /**
     * Sets the command which should be executed by the Player if he clicks
     * on the text.
     *
     * @param command the command which should be executed, the `/ should be added here
     * @param onlySuggest if true, the command won't be executed immediately,
     * instead it will be suggested in the command prompt
     */
    fun onClickCommand(command: String, onlySuggest: Boolean = false) {
        clickEvent = ClickEvent.clickEvent(
            if (onlySuggest) ClickEvent.Action.SUGGEST_COMMAND else ClickEvent.Action.RUN_COMMAND,
            command
        )
    }

    /**
     * Sets the String which should be copied to the clipboard if the
     * Player clicks on this text.
     */
    fun onClickCopy(copyText: String) {
        clickEvent = ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText)
    }

    /**
     * Adds a line break.
     */
    fun newLine() {
        siblingText.append(Component.text("\n"))
    }

    /**
     * Adds an empty line.
     */
    fun emptyLine() {
        newLine()
        newLine()
    }

    fun build() = internalText.apply {
        this@LiteralTextBuilder.bold?.let { decorate(TextDecoration.BOLD) }
        this@LiteralTextBuilder.italic?.let { decorate(TextDecoration.ITALIC) }
        this@LiteralTextBuilder.underline?.let { decorate(TextDecoration.UNDERLINED) }
        this@LiteralTextBuilder.strikethrough?.let { decorate(TextDecoration.STRIKETHROUGH) }
        this@LiteralTextBuilder.obfuscate?.let { decorate(TextDecoration.OBFUSCATED) }
        this@LiteralTextBuilder.color?.let { color(color) }
        this@LiteralTextBuilder.clickEvent?.let { clickEvent(clickEvent) }
        this@LiteralTextBuilder.hoverEvent?.let { hoverEvent(hoverEvent) }

        if (siblingText.children().isNotEmpty())
            append(siblingText)
    }
}