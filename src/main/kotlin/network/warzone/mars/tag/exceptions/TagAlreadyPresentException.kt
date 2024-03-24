package network.warzone.mars.tag.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class TagAlreadyPresentException(val player: String, val tag: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("$player already has $tag", NamedTextColor.RED)
    }
}