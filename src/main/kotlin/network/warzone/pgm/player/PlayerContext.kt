package network.warzone.pgm.player

import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.player.models.PlayerProfile
import network.warzone.pgm.player.models.Session
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.color
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.player.MatchPlayer
import java.util.*

class PlayerContext(val uuid: UUID, val player: Player, val activeSession: Session) {

    val matchPlayer: MatchPlayer
        get() = PGM.get().matchManager.getPlayer(player)!! // Can only have PlayerContext for an online player, so can be forced.

    suspend fun getPrefix(): String? {
        val rank: Rank? = getPlayerProfile().ranks()
            .filter { it.prefix != null }
            .maxByOrNull { it.priority }

        rank ?: return null

        return rank.prefix?.color()
    }

    suspend fun isStaff(): Boolean {
        return getPlayerProfile().ranks().any { it.staff }
    }

    suspend fun getPlayerProfile(): PlayerProfile {
        // The player is online so we know they exist.
        return PlayerFeature.getKnown(uuid)
    }

}