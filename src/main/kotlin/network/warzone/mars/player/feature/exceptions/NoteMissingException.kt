package network.warzone.mars.player.feature.exceptions

import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class NoteMissingException(val id: Int) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("The note $id does not exist.", NamedTextColor.RED)
    }

}