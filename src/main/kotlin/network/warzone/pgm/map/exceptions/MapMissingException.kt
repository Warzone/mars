package network.warzone.pgm.map.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.utils.FeatureException

data class MapMissingException(val map: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("Map $map does not exist.", NamedTextColor.RED)
    }

}