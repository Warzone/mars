package network.warzone.pgm.player.feature

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.feature.named.NamedCacheFeature
import network.warzone.pgm.player.PlayerContext
import network.warzone.pgm.player.PlayerManager
import network.warzone.pgm.player.models.PlayerProfile
import network.warzone.pgm.ranks.RankAttachments
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object PlayerFeature : NamedCacheFeature<PlayerProfile, PlayerService>(), Listener {
    override val service = PlayerService

    init {
        WarzonePGM.registerEvents(this)
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) = runBlocking {
        val player = event.player
        val ip = event.address.hostAddress

        val (playerProfile, activeSession) = service.login(player, ip)
        val context = PlayerManager.addPlayer(player, activeSession)

        RankAttachments.createAttachment(context)
        RankAttachments.refresh(context)

        add(playerProfile.generate())
    }

    @EventHandler
    fun onPlayerLogout(event: PlayerQuitEvent) = runBlocking {
        val player = event.player
        val uuid = player.uniqueId

        val activeSession = PlayerManager.getPlayer(uuid)?.activeSession ?: return@runBlocking

        val context: PlayerContext = PlayerManager.removePlayer(uuid)!! // We know the player is online
        RankAttachments.removeAttachment(context)

        cache.remove(uuid)

        val sessionLength = Date().time - activeSession.createdAt.time
        service.logout(uuid, sessionLength)
    }

}