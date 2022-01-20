package network.warzone.mars.player.decoration

import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.utils.color
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.util.named.NameDecorationProvider
import tc.oc.pgm.util.text.TextTranslations
import java.util.*

class PrefixDecorationProvider : NameDecorationProvider {

    override fun getPrefix(uuid: UUID): String = runBlocking {
        val player = PlayerManager.getPlayer(uuid)
        val prefix: String = player?.getPrefix() ?: return@runBlocking ""
        // Add a space after the prefix
        return@runBlocking prefix.color() + " "
    }

    override fun getSuffix(uuid: UUID?): String = ""

}