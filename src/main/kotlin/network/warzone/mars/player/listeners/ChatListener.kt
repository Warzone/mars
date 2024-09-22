package network.warzone.mars.player.listeners

import github.scarsz.discordsrv.DiscordSRV
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import network.warzone.mars.Mars
import network.warzone.mars.api.socket.models.ChatChannel
import network.warzone.mars.api.socket.models.MessageEvent
import network.warzone.mars.api.socket.models.PlayerChatEvent
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.LevelColorService
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.models.PunishmentKind
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.utils.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.Permissions
import tc.oc.pgm.api.integration.Integration
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.player.MatchPlayer
import tc.oc.pgm.api.setting.SettingKey
import tc.oc.pgm.api.setting.SettingValue
import tc.oc.pgm.listeners.ChatDispatcher
import tc.oc.pgm.util.bukkit.OnlinePlayerMapAdapter


class ChatListener : Listener {
    class MatchPlayerChatEvent(
        val matchPlayer: MatchPlayer,
        val channel: ChatChannel,
        val message: String
    ) : KEvent()

    companion object {
        val CHANNEL_VALUE_MAP = mapOf(
            listOf("g", "all") to SettingValue.CHAT_GLOBAL,
            listOf("t") to SettingValue.CHAT_TEAM,
            listOf("a") to SettingValue.CHAT_ADMIN
        )
        var DISCORDSRV_ENABLED = false
    }

    private val queuedChannels: OnlinePlayerMapAdapter<SettingValue> = OnlinePlayerMapAdapter(Mars.get())

    init {
        try {
            Class.forName("github.scarsz.discordsrv.DiscordSRV")
            DISCORDSRV_ENABLED = Bukkit.getPluginManager().getPlugin("DiscordSRV") != null
        } catch (e: ClassNotFoundException) {
            Mars.get().logger.warning("DiscordSRV could not be found, disabling DiscordSRV integration")
        }
        LevelColorService
        HandlerList.unregisterAll(ChatDispatcher.get())
    }

    @EventHandler
    fun onMessage(event: MessageEvent) {
        val (rawMessage, soundName, playerIds) = event.data
        val message = translateAlternateColorCodes('&', rawMessage)
        if (soundName != null) {
            try {
                val sound = Sound.valueOf(soundName)
                playerIds.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    val location = it.location
                    it.playSound(location, sound, 1000f, 1f)
                    it.sendMessage(message)
                }
            } catch (e: Exception) {
                Bukkit.getLogger()
                    .warning("Exception occurred receiving MESSAGE socket event - Sound: $soundName, Message: $rawMessage\n${e.printStackTrace()}")
            }
        } else {
            playerIds.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                it.sendMessage(message)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPreCommand(event: PlayerCommandPreprocessEvent) {
        val message = event.message.toLowerCase().substring(1).split(" ")
        if (message.size == 1) return // Channel should be persistent
        val matchPlayer = PGM.get().matchManager.getPlayer(event.player)
        val oldChannel: SettingValue = matchPlayer?.settings?.getValue(SettingKey.CHAT) ?: SettingValue.CHAT_GLOBAL
        for (command in CHANNEL_VALUE_MAP) {
            for (alias in command.key) {
                if (message[0] == alias || message[0] == "pgm:$alias") {
                    queuedChannels[event.player] = oldChannel
                    matchPlayer?.settings?.setValue(SettingKey.CHAT, command.value)
                    break
                }
            }
        }
    }

    @EventHandler
    fun onPlayerChatApi(event: PlayerChatEvent) {
        val (player, prefix, channel, message, serverId) = event.data

        if (channel != ChatChannel.STAFF) return

        sendAdminChat(PGM.get().matchManager.getMatch(), prefix, player.name, message, serverId)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) = runBlocking {
        val player = event.player
        val context = PlayerManager.getPlayer(player.uniqueId)!!

        val match = context.matchPlayer.match

        context.activePunishments = context.activePunishments.filter { it.isActive }

        val activeMute =
            context.activePunishments.filter { it.action.kind == PunishmentKind.MUTE }.maxByOrNull { it.issuedAt }

        if (activeMute != null) {
            event.isCancelled = true
            val appealLink = Mars.get().config.getString("server.links.appeal")
                ?: throw RuntimeException("No appeal link set in config")
            if (activeMute.action.isPermanent()) player.sendMessage("${GRAY}You are muted for ${RED}${activeMute.reason.name}${GRAY}. $RED${activeMute.reason.message} ${GRAY}You may appeal at ${AQUA}$appealLink")
            else player.sendMessage("${GRAY}You are muted for ${RED}${activeMute.reason.name} ${GRAY}until ${WHITE}${activeMute.expiresAt} (${activeMute.expiresAt.getRelativeTime()})${GRAY}. $RED${activeMute.reason.message} ${GRAY}You may appeal at ${AQUA}$appealLink")
            return@runBlocking
        }
        val chatChannel =
            if (Integration.isVanished(context.player) && context.player.hasPermission(Permissions.ADMINCHAT)) SettingValue.CHAT_ADMIN
            else context.matchPlayer.settings.getValue(SettingKey.CHAT)

        val isChatEnabled = Mars.get().config.getBoolean("chat.enabled")
        if (chatChannel == SettingValue.CHAT_GLOBAL && !isChatEnabled && !player.hasPermission("mars.chat.mute.bypass")) {
            player.sendMessage("${RED}Global chat is currently disabled.")
            event.isCancelled = true
            return@runBlocking
        }

        when (chatChannel) {
            SettingValue.CHAT_ADMIN -> sendAdminChat(match, context.getPrefix() ?: "", player.name, event.message, null)
            SettingValue.CHAT_TEAM -> sendTeamChat(context.matchPlayer.party, context, event.message)
            else -> sendGlobalChat(match, context, event.message)
        }

        val matchPlayer = PGM.get().matchManager.getPlayer(player)!!

        MatchPlayerChatEvent(
            matchPlayer,
            when (chatChannel) {
                SettingValue.CHAT_ADMIN -> ChatChannel.STAFF
                SettingValue.CHAT_TEAM -> ChatChannel.TEAM
                else -> ChatChannel.GLOBAL
            },
            event.message
        ).callEvent()

        event.isCancelled = true
        if (queuedChannels.containsKey(event.player))
            matchPlayer.settings.setValue(SettingKey.CHAT, queuedChannels.remove(event.player))
    }

    private suspend fun sendGlobalChat(match: Match, context: PlayerContext, message: String) {
        val prefix = context.getPrefix()
        val teamColor = context.matchPlayer.party.fullColor
        val username = context.player.name

        val profile = context.getPlayerProfile()
        val tag = profile.activeTag()

        val messageBuilder = text()

        messageBuilder.append { getPlayerLevelAsComponent(profile) }.append(space())

        if (prefix != null) messageBuilder.append { getRanksHoverComponent(text("$prefix "), profile) }

        messageBuilder.append { getRanksHoverComponent(text(username, TextColor.color(teamColor.red, teamColor.green, teamColor.blue)), profile) }

        if (tag != null) messageBuilder.append { text(" $GRAY[${tag.display.color()}$GRAY]") }

        messageBuilder.append { text(": ", NamedTextColor.WHITE) }
        messageBuilder.append { text(message, NamedTextColor.WHITE) }

        val messageComponent = messageBuilder.build()

        match.sendMessage(messageComponent)
        sendToDiscord(context.player, message)
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

    private fun sendToDiscord(player: Player, message: String) {
        if (!DISCORDSRV_ENABLED) return
        Bukkit.getScheduler().runTaskAsynchronously(Mars.get()) {
            DiscordSRV.getPlugin().processChatMessage(
                player,
                message,
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
            )
        }
    }

    private fun getRanksHoverComponent(component: Component, profile: PlayerProfile) : Component {
        val cachedRanks = profile.rankIds.mapNotNull { RankFeature.getCached(it) }
        if (cachedRanks.isEmpty()) return component
        val hoverComponent = text()
        hoverComponent.append(Component.text("Ranks:", NamedTextColor.GRAY))
        cachedRanks.forEach { cachedRank ->
            val name = cachedRank.prefix?.color() ?: cachedRank.displayName?.color() ?: cachedRank.name
            hoverComponent.appendNewline().append(text(name))
        }
        return component.hoverEvent(hoverComponent.build())
    }
}
