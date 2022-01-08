package network.warzone.mars.player.decoration

import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.util.named.NameDecorationProvider
import tc.oc.pgm.util.text.TextTranslations
import java.util.*

class PrefixDecorationProvider : NameDecorationProvider {

    override fun getPrefix(player: UUID?): String = TextTranslations.translateLegacy(
        // TODO: Get prefix from player ranks
        text("MOD", NamedTextColor.YELLOW, TextDecoration.BOLD).append(text(" ")), null
    )

    override fun getSuffix(p0: UUID?): String = ""

}