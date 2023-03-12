package network.warzone.mars.report

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import network.warzone.mars.feature.Resource
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.util.named.NameStyle
import java.time.Instant
import java.util.*

class Report(
    override val _id: UUID = UUID.randomUUID(),
    val target: UUID,
    val sender: UUID,
    val targetName: String, // Offline name
    val senderName: String, // Offline name
    val reason: String,
    val timeSent: Instant = Instant.now()
) : Resource, Comparable<Report> {

    fun getUsername(uuid: UUID, match: Match): Component {
        val player: MatchPlayer? = match.getPlayer(uuid);
        player ?: return text(
            if (uuid == target) targetName else senderName,
            NamedTextColor.DARK_AQUA,
            TextDecoration.ITALIC
        )
        return player.getName(NameStyle.FANCY);
    }

    fun getTargetComponent(match: Match): Component {
        return getUsername(this.target, match)
    }

    fun getSenderComponent(match: Match): Component {
        return getUsername(this.sender, match)
    }

    override fun compareTo(other: Report): Int = timeSent.compareTo(other.timeSent)
}
