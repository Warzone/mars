package network.warzone.mars.player.achievements.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException
import java.util.*

class AchievementMissingException(val id: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("An achievement with id $id does not exist.", NamedTextColor.RED)
    }
}