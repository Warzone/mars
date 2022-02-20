package network.warzone.mars.punishment.exceptions

import network.warzone.mars.utils.FeatureException
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

data class PunishmentAlreadyRevertedException(val id: String) : FeatureException() {
    override fun asComponent(): Component {
        return translatable("exception.punishment.reverted", NamedTextColor.RED, text(id))
    }
}
