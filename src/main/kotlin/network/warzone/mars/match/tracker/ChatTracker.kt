package network.warzone.mars.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.OutboundEvent
import network.warzone.mars.api.socket.models.PlayerChatData
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.listeners.ChatListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatTracker : Listener {
    @EventHandler
    fun onPlayerChat(event: ChatListener.MatchPlayerChatEvent) = runBlocking {
        val context: PlayerContext = PlayerManager.getPlayer(event.matchPlayer.id)!!

        ApiClient.emit(
            OutboundEvent.PlayerChat, PlayerChatData(
                event.matchPlayer.id,
                event.matchPlayer.nameLegacy,
                context.getPrefix() ?: "",
                event.channel,
                event.message,
                Mars.instance.serverId
            )
        )
    }
}