package network.warzone.mars.broadcast.exceptions

import network.warzone.mars.utils.FeatureException
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

data class BroadcastMissingException(val name: String) : FeatureException() {
    override fun asComponent(): Component {
        return translatable("exception.broadcast.not-exist", NamedTextColor.RED, text(name))
    }
}