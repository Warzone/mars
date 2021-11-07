package network.warzone.mars.tag.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.FeatureException

data class TagNotPresentException(val player: PlayerContext, val tag: Tag) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("${player.player.name} does not have ${tag.name}", NamedTextColor.RED)
    }

}