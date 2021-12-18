package network.warzone.mars.tag.exceptions

import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class TagMissingException(val name: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("The tag $name does not exist.", NamedTextColor.RED)
    }

}
