package network.warzone.mars.player.feature.exceptions

import network.warzone.mars.utils.FeatureException
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

data class PlayerNotOnlineException(val target: String) : FeatureException() {
    override fun asComponent(): Component {
        return translatable("exception.player.not-online", NamedTextColor.RED, text(target))
    }
}