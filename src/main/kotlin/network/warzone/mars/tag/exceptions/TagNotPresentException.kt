package network.warzone.mars.tag.exceptions

import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.FeatureException

data class TagNotPresentException(val player: PlayerContext, val tag: Tag) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("${player.player.name} does not have ${tag.name}", NamedTextColor.RED)
    }

}