package network.warzone.mars.player.listeners

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.ChatChannel
import network.warzone.mars.api.socket.models.MessageEvent
import network.warzone.mars.api.socket.models.PlayerChatEvent
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.punishment.models.PunishmentKind
import network.warzone.mars.utils.KEvent
import network.warzone.mars.utils.color
import network.warzone.mars.utils.getMatch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.Permissions
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.setting.SettingKey
import tc.oc.pgm.api.setting.SettingValue
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.text
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import tc.oc.pgm.lib.net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import tc.oc.pgm.lib.net.kyori.adventure.text.Component.space
import java.util.*

class ChatListener : Listener {

    class MatchPlayerChatEvent(
        val matchPlayer: MatchPlayer,
        val channel: ChatChannel,
        val message: String
    ) : KEvent()

    @EventHandler
    fun onMessage(event: MessageEvent) {
        val (rawMessage, soundName, players) = event.data
        val message = translateAlternateColorCodes('&', rawMessage)
        if (soundName != null) {
            try {
                val sound = Sound.valueOf(soundName)
                players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    val location = it.location
                    it.playSound(location, sound, 1000f, 1f)
                    it.sendMessage(message)
                }
            } catch (e: Exception) {
                Bukkit.getLogger()
                    .warning("Exception occurred receiving MESSAGE socket event - Sound: $soundName, Message: $rawMessage")
            }
        } else {
            players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                it.sendMessage(message)
            }
        }
    }

    @EventHandler
    fun onPlayerChatApi(event: PlayerChatEvent) {
        val (_, playerName, playerPrefix, channel, message, serverId) = event.data

        if (channel != ChatChannel.STAFF) return

        sendAdminChat(PGM.get().matchManager.getMatch(), playerPrefix, playerName, message, serverId)
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) = runBlocking {
        val player = event.player
        val context = PlayerManager.getPlayer(player.uniqueId)!!

        val match = context.matchPlayer.match

        println(context.activePunishments)

        context.activePunishments = context.activePunishments.filter { it.isActive }

        val activeMute =
            context.activePunishments.filter { it.action.kind == PunishmentKind.MUTE }.maxByOrNull { it.issuedAt }

        if (activeMute != null) {
            event.isCancelled = true
            if (activeMute.action.isPermanent()) player.sendMessage("${GRAY}You are muted for ${RED}${activeMute.reason.name}${GRAY}. $RED${activeMute.reason.message} ${GRAY}You may appeal at ${AQUA}https://warzone.network/appeal")
            else player.sendMessage("${GRAY}You are muted for ${RED}${activeMute.reason.name} ${GRAY}until ${WHITE}${activeMute.expiresAt}${GRAY}. $RED${activeMute.reason.message} ${GRAY}You may appeal at ${AQUA}https://warzone.network/appeal")
            return@runBlocking
        }

        val chatChannel = context.matchPlayer.settings.getValue(SettingKey.CHAT)
        when (chatChannel) {
            SettingValue.CHAT_ADMIN -> sendAdminChat(match, context.getPrefix() ?: "", player.name, event.message, null)
            SettingValue.CHAT_TEAM -> sendTeamChat(context.matchPlayer.party, context, event.message)
            else -> sendGlobalChat(match, context, event.message)
        }

        MatchPlayerChatEvent(
            PGM.get().matchManager.getPlayer(player)!!,
            when (chatChannel) {
                SettingValue.CHAT_ADMIN -> ChatChannel.STAFF
                SettingValue.CHAT_TEAM -> ChatChannel.TEAM
                else -> ChatChannel.GLOBAL
            },
            event.message
        ).callEvent()

        event.isCancelled = true
    }

    @EventHandler
    private suspend fun sendGlobalChat(match: Match, context: PlayerContext, message: String) {
        val prefix = context.getPrefix()
        val teamColor = context.matchPlayer.party.fullColor
        val username = context.player.name

        val profile = context.getPlayerProfile()
        val tag = profile.activeTag()

        val messageBuilder = text()

        // todo: level colours
        messageBuilder.append { text("[${profile.stats.level}]", NamedTextColor.GRAY) }.append(space())

        if (prefix != null) messageBuilder.append { text("$prefix ") }

        messageBuilder.append { text(username, TextColor.color(teamColor.red, teamColor.green, teamColor.blue)) }

        if (tag != null) messageBuilder.append { text(" $GRAY[${tag.display.color()}$GRAY]") }

        messageBuilder.append { text(": ", NamedTextColor.WHITE) }
        messageBuilder.append { text(message, NamedTextColor.WHITE) }

        val messageComponent = messageBuilder.build()

        match.sendMessage(messageComponent)
    }

    private fun sendTeamChat(team: Party, context: PlayerContext, message: String) {
        val teamName = team.defaultName
        val username = context.player.name

        val messageComponent = text()
            .append { text("[$teamName] $username", TextColor.color(team.fullColor.asRGB())) }
            .append { text(": $message", NamedTextColor.WHITE) }
            .build()

        team.sendMessage(messageComponent)
    }

    private fun sendAdminChat(match: Match, prefix: String, username: String, message: String, serverId: String?) {
        val coloredPrefix = prefix.color()

        val magicSpace = if (prefix == "") "" else " "
        val server = if (serverId != null) "${LIGHT_PURPLE}@$serverId${GRAY}" else ""

        match.players
            .map { it.bukkit }
            .filter { it.hasPermission(Permissions.ADMINCHAT) }
            .forEach { it.sendMessage("$DARK_RED[STAFF] $RESET$coloredPrefix$magicSpace$GRAY$username$server: $GREEN$message") }
    }

}