package network.warzone.mars.broadcast.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class BroadcastMissingException(val name: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("The broadcast $name does not exist.", NamedTextColor.RED)
    }
}