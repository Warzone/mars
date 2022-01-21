package network.warzone.mars.player.decoration

import kotlinx.coroutines.runBlocking
import network.warzone.mars.player.PlayerManager
import tc.oc.pgm.util.named.NameDecorationProvider
import java.util.*

class PrefixDecorationProvider : NameDecorationProvider {

    override fun getPrefix(uuid: UUID): String = runBlocking {
        val player = PlayerManager.getPlayer(uuid)
        val prefix: String = player?.getPrefix() ?: return@runBlocking ""
        // Add a space after the prefix
        return@runBlocking "$prefix "
    }

    override fun getSuffix(uuid: UUID?): String = ""

}