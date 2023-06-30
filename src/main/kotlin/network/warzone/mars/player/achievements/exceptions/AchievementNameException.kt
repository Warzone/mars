package network.warzone.mars.player.achievements.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

class AchievementNameException(val name: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("An achievement by the name $name already exists.", NamedTextColor.RED)
    }
}