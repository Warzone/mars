package network.warzone.mars.rank.exceptions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.FeatureException

data class RankAlreadyPresentException(val player: PlayerContext, val rank: Rank) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("${player.player.name} already has ${rank.name}", NamedTextColor.RED)
    }

}