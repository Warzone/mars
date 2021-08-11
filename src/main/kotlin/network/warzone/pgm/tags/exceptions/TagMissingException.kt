package network.warzone.pgm.tags.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.utils.FeatureException

data class TagMissingException(val name: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("The tag $name does not exist.", NamedTextColor.RED)
    }

}
