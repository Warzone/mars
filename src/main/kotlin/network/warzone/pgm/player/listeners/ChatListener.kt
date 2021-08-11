package network.warzone.pgm.player.listeners

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.player.PlayerContext
import network.warzone.pgm.player.PlayerManager
import org.bukkit.ChatColor.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import tc.oc.pgm.api.Permissions
import tc.oc.pgm.api.match.Match
import tc.oc.pgm.api.party.Party
import tc.oc.pgm.api.setting.SettingKey
import tc.oc.pgm.api.setting.SettingValue

class ChatListener : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) = runBlocking {
        val player = event.player
        val context = PlayerManager.getPlayer(player.uniqueId)!!

        val match = context.matchPlayer.match

        when (context.matchPlayer.settings.getValue(SettingKey.CHAT)) {
            SettingValue.CHAT_ADMIN -> sendAdminChat(match, context, event.message)
            SettingValue.CHAT_TEAM -> sendTeamChat(context.matchPlayer.party, context, event.message)
            else -> sendGlobalChat(match, context, event.message)
        }

        event.isCancelled = true
    }

    private suspend fun sendGlobalChat(match: Match, context: PlayerContext, message: String) {

        //TODO: tags, levels

        val prefix = context.getPrefix()
        val teamColor = context.matchPlayer.party.color
        val username = context.player.name

        val magicSpace = if (prefix == "") "" else " "

        match.players
            .map { it.bukkit }
            .forEach { it.sendMessage("$prefix$magicSpace$teamColor$username$WHITE: $message") }
    }

    private fun sendTeamChat(team: Party, context: PlayerContext, message: String) {
        val teamColor = team.color
        val teamName = team.defaultName

        val username = context.player.name

        team.players
            .map { it.bukkit }
            .forEach { it.sendMessage("$teamColor[$teamName] $username$WHITE: $message") }
    }

    private suspend fun sendAdminChat(match: Match, context: PlayerContext, message: String) {
        val prefix = context.getPrefix()
        val username = context.player.name

        val magicSpace = if (prefix == "") "" else " "

        match.players
            .map { it.bukkit }
            .filter { it.hasPermission(Permissions.ADMINCHAT) }
            .forEach { it.sendMessage("$DARK_RED[STAFF] $RESET$prefix$magicSpace$GRAY$username: $GREEN$message") }
    }



}