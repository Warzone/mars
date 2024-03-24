package network.warzone.mars.rank.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.utils.FeatureException

data class RankAlreadyPresentException(val player: String, val rank: String) : FeatureException() {
    override fun asTextComponent(): TextComponent {
        return Component.text("$player already has $rank", NamedTextColor.RED)
    }
}