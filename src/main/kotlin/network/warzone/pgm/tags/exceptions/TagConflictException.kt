package network.warzone.pgm.tags.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.pgm.utils.FeatureException

data class TagConflictException(val name: String) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("A tag by the name $name already exists.", NamedTextColor.RED)
    }

}
