package network.warzone.mars.player.tablist

import network.warzone.mars.Mars
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import tc.oc.pgm.lib.net.kyori.adventure.text.Component
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextDecoration
import tc.oc.pgm.tablist.*
import tc.oc.pgm.teams.Team
import tc.oc.pgm.util.named.NameDecorationProvider
import tc.oc.pgm.util.named.NameStyle
import tc.oc.pgm.util.tablist.PlayerTabEntry
import tc.oc.pgm.util.tablist.TabView
import tc.oc.pgm.util.text.PlayerComponent

class TeamTabEntryImpl(team: Team) : TeamTabEntry(team) // TeamTabEntry's constructor is protected

class LeveledPlayerTabEntry(player: Player) : PlayerTabEntry(player) {

    override fun getContent(view: TabView?): Component {
        return tabPlayer(player, view!!.viewer)
    }

    companion object {
        private val style = listOf(
            NameStyle.Flag.COLOR,
            NameStyle.Flag.SELF,
            NameStyle.Flag.DISGUISE,
            NameStyle.Flag.DEATH
        )
    }

    private fun tabPlayer(
        player: Player?, viewer: Player?
    ): Component {
        val isOffline = player == null || !player.isOnline
        var provider = NameDecorationProvider.DEFAULT
        if (player != null) {
            val metadata = player.getMetadata(NameDecorationProvider.METADATA_KEY, PGM.get())
            if (metadata != null) provider = metadata.value() as NameDecorationProvider
        }
        val uuid = if (!isOffline) player!!.uniqueId else null
        val builder = Component.text()
        if (!isOffline) { // Add levels
            // TODO: Display real level
            builder.color(NamedTextColor.GRAY).append(Component.text("[1] "))
        }
        val name = Component.text().content(
            (if (player != null) player.name else "Unknown")!!
        )
        if (!isOffline && style.contains(NameStyle.Flag.DEATH) && PlayerComponent.isDead(
                player
            )
        ) {
            name.color(NamedTextColor.DARK_GRAY)
        } else if (style.contains(NameStyle.Flag.COLOR)) {
            name.color(if (isOffline) PlayerComponent.OFFLINE_COLOR else provider.getColor(uuid))
        }
        if (!isOffline && style.contains(NameStyle.Flag.SELF) && player === viewer) {
            name.decoration(TextDecoration.BOLD, true)
        }
        if (!isOffline && style.contains(NameStyle.Flag.DISGUISE) && PlayerComponent.isDisguised(
                player
            )
        ) {
            name.decoration(TextDecoration.STRIKETHROUGH, true)
        }
        builder.append(name)
        if (style.contains(NameStyle.Flag.FLAIR) && !isOffline) {
            builder.append(provider.getSuffixComponent(uuid))
        }
        return builder.build()
    }

}

fun Mars.overrideTabManager() {
    PGM.get().matchTabManager?.disable()
    this.matchTabManager = MatchTabManager(
        this,
        ::LeveledPlayerTabEntry,
        ::TeamTabEntryImpl,
        ::MapTabEntry,
        { arrayOf(
            MapTabEntry(it),
            AuthorTabEntry(it, 0),
            AuthorTabEntry(it, 1),
            MatchFooterTabEntry(it)
        ) },
        ::MatchFooterTabEntry,
        ::FreeForAllTabEntry
    )
    Mars.registerEvents(this.matchTabManager)
}