package network.warzone.mars.rank.exceptions

import network.warzone.mars.player.PlayerContext
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.FeatureException
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.translatable
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor

data class RankNotPresentException(val player: String, val rank: String) : FeatureException() {
    override fun asComponent(): Component {
        return translatable("exception.rank.player.not-present", NamedTextColor.RED, text(player), text(rank))
    }
}