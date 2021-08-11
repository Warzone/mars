package network.warzone.pgm.player.feature.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.utils.FeatureException

data class PlayerMissingException(val target: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("The player $target does not exist.", NamedTextColor.RED)
    }

}