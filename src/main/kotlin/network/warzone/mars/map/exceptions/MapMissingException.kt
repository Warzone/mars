package network.warzone.mars.map.exceptions

import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class MapMissingException(val map: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("Map $map does not exist.", NamedTextColor.RED)
    }
}