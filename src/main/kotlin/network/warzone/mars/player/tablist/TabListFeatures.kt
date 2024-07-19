package network.warzone.mars.player.tablist

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import network.warzone.mars.Mars
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.AUDIENCE_PROVIDER
import network.warzone.mars.utils.getLevelAsComponent
import network.warzone.mars.utils.getPlayerLevelAsComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.integration.Integration
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.player.MatchPlayerState
import tc.oc.pgm.util.Players
import tc.oc.pgm.util.named.NameDecorationProvider
import tc.oc.pgm.util.named.NameStyle
import tc.oc.pgm.util.player.PlayerComponent
import tc.oc.pgm.util.player.PlayerComponent.player
import tc.oc.pgm.util.player.PlayerRenderer
import tc.oc.pgm.util.tablist.PlayerTabEntry
import tc.oc.pgm.util.text.RenderableComponent
import java.util.*


fun Mars.overrideTabManager() {
    AUDIENCE_PROVIDER
    PlayerTabEntry.setPlayerComponent(::LeveledPlayerComponent)
}

class LeveledPlayerRenderer {
    companion object {
        val NICK_STYLE: Style = Style.style(TextDecoration.ITALIC).decoration(
            TextDecoration.STRIKETHROUGH, false
        )
    }

    fun render(player: Player?, data: PlayerData, relation: PlayerRelationship): Component {
        if (data.name == null) return PlayerComponent.UNKNOWN

        var disguised = (data.nick != null || data.vanish)

        if (!data.online || (data.conceal && disguised && !relation.reveal)) {
            return text(data.name, PlayerRenderer.OFFLINE_COLOR)
        }

        var plName = if (relation.reveal || data.nick == null) data.name else data.nick

        var color =
            if (data.style.contains(NameStyle.Flag.DEATH) && data.dead
            ) PlayerRenderer.DEAD_COLOR
            else if (data.style.contains(NameStyle.Flag.COLOR)) data.teamColor else null

        var name = text().content(plName).color(color)
        val uuid = data.uuid

        if (relation.reveal && data.style.contains(NameStyle.Flag.SELF) && relation.self) {
            name.decoration(TextDecoration.BOLD, true)
        }
        if (relation.reveal && data.style.contains(NameStyle.Flag.FRIEND) && relation.friend) {
            name.decoration(TextDecoration.ITALIC, true)
        }
        if (data.style.contains(NameStyle.Flag.SQUAD) && relation.squad) {
            name.decoration(TextDecoration.UNDERLINED, true)
        }
        if (relation.reveal && data.style.contains(NameStyle.Flag.DISGUISE) && disguised) {
            name.decoration(TextDecoration.STRIKETHROUGH, true)

            if (data.nick != null && data.style.contains(NameStyle.Flag.NICKNAME)) {
                name.append(text(" " + data.nick, NICK_STYLE))
            }
        }

        val component = if (relation.reveal && data.style.contains(NameStyle.Flag.FLAIR)) {
            var provider: NameDecorationProvider = PGM.get().nameDecorationRegistry
            textOfChildren(
                provider.getPrefixComponent(uuid), name, provider.getSuffixComponent(uuid)
            )
        } else {
            name.build()
        }

        val profile = PlayerFeature.getCached(player!!.uniqueId)
        val level = if (profile != null) getPlayerLevelAsComponent(profile) else getLevelAsComponent(1)
        return level.append(space()).append(component)
    }
}

class LeveledPlayerComponent(
    private val player: Player?,
    private val nameStyle: NameStyle,
) : RenderableComponent {

    companion object {
        val RENDERER = LeveledPlayerRenderer()
    }

    constructor(player: Player?) : this(player, NameStyle.TAB)

    override fun render(viewer: CommandSender): Component {
        if (player == null) return PlayerComponent.UNKNOWN_PLAYER
        return RENDERER.render(player, PlayerData(player, NameStyle.TAB), PlayerRelationship(player, viewer))
    }
}

fun getFlags(nameStyle: NameStyle): Set<NameStyle.Flag> {
    return NameStyle.Flag.values().filter(nameStyle::has).toSet()
}

fun styleToSet(style: NameStyle): Set<NameStyle.Flag> {
    return when (style) {
        NameStyle.TAB -> getFlags(style) - NameStyle.Flag.FLAIR
        else -> getFlags(style)
    }
}

// Replicated protected classes from PGM
class PlayerData {
    val uuid: UUID?

    val name: String?
    val nick: String?
    val teamColor: TextColor
    val dead: Boolean
    val vanish: Boolean
    val online: Boolean
    val conceal: Boolean
    val style: Set<NameStyle.Flag>

    constructor(player: Player, style: NameStyle) {
        this.uuid = player.uniqueId
        this.name = player.name
        this.nick = Integration.getNick(player)
        var mp = PGM.get().matchManager.getPlayer(player)
        this.teamColor = if (mp == null) PlayerRenderer.OFFLINE_COLOR else mp.party.textColor
        this.dead = mp != null && mp.isDead
        this.vanish = Integration.isVanished(player)
        this.online = player.isOnline
        this.conceal = false

        this.style = styleToSet(style)
    }

    constructor(mp: MatchPlayer, style: NameStyle) {
        this.uuid = mp.id

        this.name = mp.nameLegacy
        this.nick = Integration.getNick(mp.bukkit)
        this.teamColor = if (mp.party == null) PlayerRenderer.OFFLINE_COLOR else mp.party.textColor
        this.dead = mp.isDead
        this.vanish = Integration.isVanished(mp.bukkit)
        this.online = mp.bukkit.isOnline
        this.conceal = false
        this.style = styleToSet(style)
    }

    constructor(mps: MatchPlayerState, style: NameStyle) {
        this.uuid = mps.id

        this.name = mps.nameLegacy
        this.nick = mps.nick
        this.teamColor = mps.party.textColor
        this.dead = mps.isDead
        this.vanish = mps.isVanished
        this.online = true
        this.conceal = false
        this.style = styleToSet(style)
    }

    constructor(player: Player?, username: String?, style: NameStyle) {
        this.uuid = if (player != null) player.uniqueId else null

        this.name = if (player != null) player.name else username
        this.nick = if (player != null) Integration.getNick(player) else null
        // Null-check is relevant as MatchManager will be null when loading author names.
        var mp = if (player != null) PGM.get().matchManager.getPlayer(player) else null
        this.teamColor = if (mp == null) PlayerRenderer.OFFLINE_COLOR else mp.party.textColor
        this.dead = mp != null && mp.isDead
        this.vanish = mp != null && Integration.isVanished(mp.bukkit)
        this.online = player != null && player.isOnline
        this.conceal = true
        this.style = styleToSet(style)
    }

    public override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (!(o is PlayerData)) return false

        var that = o

        if (dead != that.dead) return false
        if (vanish != that.vanish) return false
        if (online != that.online) return false
        if (conceal != that.conceal) return false
        if (!(uuid == that.uuid)) return false
        if (!(name == that.name)) return false
        if (!(nick == that.nick)) return false
        if (!(teamColor == that.teamColor)) return false
        return style == that.style
    }
}

class PlayerRelationship(pl: Player?, viewer: CommandSender) {
    val reveal: Boolean = pl != null && Players.shouldReveal(viewer, pl)
    val self: Boolean = pl === viewer
    val friend: Boolean = pl != null && Players.isFriend(viewer, pl)
    val squad: Boolean = pl != null && viewer is Player && Integration.areInSquad(viewer, pl)

    public override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        var that = o as PlayerRelationship

        if (reveal != that.reveal) return false
        if (self != that.self) return false
        if (friend != that.friend) return false
        return squad == that.squad
    }
}