package network.warzone.mars.tag.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class TagNotPresentException(val player: String, val tag: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("$player does not have $tag", NamedTextColor.RED)
    }
}