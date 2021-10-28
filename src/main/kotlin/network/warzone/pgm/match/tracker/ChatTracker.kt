package network.warzone.pgm.match.tracker

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.api.socket.OutboundEvent
import network.warzone.pgm.api.socket.models.PlayerChatData
import network.warzone.pgm.player.PlayerContext
import network.warzone.pgm.player.PlayerManager
import network.warzone.pgm.player.listeners.ChatListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatTracker : Listener {

    @EventHandler
    fun onPlayerChat(event: ChatListener.MatchPlayerChatEvent) = runBlocking {
        val context: PlayerContext = PlayerManager.getPlayer(event.matchPlayer.id)!!

        ApiClient.emit(OutboundEvent.PlayerChat, PlayerChatData(
            event.matchPlayer.id,
            event.matchPlayer.nameLegacy,
            context.getPrefix() ?: "",
            event.channel,
            event.message,
            WarzonePGM.instance.serverId
        ))
    }

}