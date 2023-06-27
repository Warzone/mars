package network.warzone.mars.player.achievements.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

// TODO: Change this to be more descriptive when necessary.
class AchievementException : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("An error has occurred related to Achievements.", NamedTextColor.RED)
    }
}