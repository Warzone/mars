package network.warzone.mars.tag.exceptions

import network.warzone.mars.utils.FeatureException
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

data class TagNotPresentException(val player: String, val tag: String) : FeatureException() {
    override fun asComponent(): Component {
        return translatable("exception.tag.not-present", NamedTextColor.RED, text(player), text(tag))
    }
}