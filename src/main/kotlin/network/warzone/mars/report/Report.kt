package network.warzone.mars.report

import net.kyori.adventure.text.Component
import network.warzone.mars.feature.Resource
import tc.oc.pgm.api.match.Match
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
        return network.warzone.mars.utils.getUsername(
            uuid,
            if (uuid == target) targetName else senderName,
            match
        )
    }

    fun getTargetComponent(match: Match): Component {
        return getUsername(this.target, match)
    }

    fun getSenderComponent(match: Match): Component {
        return getUsername(this.sender, match)
    }

    override fun compareTo(other: Report): Int = timeSent.compareTo(other.timeSent)
}
