package network.warzone.pgm.tags.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.player.PlayerContext
import network.warzone.pgm.tags.models.Tag
import network.warzone.pgm.utils.FeatureException

data class TagAlreadyPresentException(val player: PlayerContext, val tag: Tag) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("${player.player.name} already has ${tag.name}", NamedTextColor.RED)
    }

}