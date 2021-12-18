package network.warzone.mars.rank.exceptions

import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.FeatureException

data class RankNotPresentException(val player: PlayerContext, val rank: Rank) : FeatureException() {

    override fun asTextComponent(): TextComponent {
        return Component.text("${player.player.name} does not have ${rank.name}", NamedTextColor.RED)
    }

}