package network.warzone.pgm.player

import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.player.models.PlayerProfile
import network.warzone.pgm.player.models.Session
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.player.MatchPlayer
import java.util.*

class PlayerContext(val uuid: UUID, val player: Player, val activeSession: Session) {

    val matchPlayer: MatchPlayer
        get() = PGM.get().matchManager.getPlayer(player)!! // Can only have PlayerContext for an online player, so can be forced.

    suspend fun getPlayerProfile(): PlayerProfile {
        return PlayerFeature.get(uuid)
    }

}