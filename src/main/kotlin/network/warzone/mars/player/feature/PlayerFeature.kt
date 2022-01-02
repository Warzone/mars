package network.warzone.mars.player.feature

import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.NamedCachedFeature
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.commands.PlayerCommands
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.Session
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.RankAttachments
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.color
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object PlayerFeature : NamedCachedFeature<PlayerProfile>(), Listener {
    private val queuedJoins = hashMapOf<UUID, Triple<PlayerProfile, Session, List<Punishment>>>()

    init {
        Mars.registerEvents(this)
    }

    override suspend fun fetch(target: String): PlayerProfile? {
        return try {
            ApiClient.get<PlayerProfile>("/mc/players/$target")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addRank(player: String, rank: Rank): PlayerProfile {
        val profile = PlayerService.addRank(player, rank)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
            RankAttachments.refresh(it)
        }

        return profile
    }

    suspend fun removeRank(player: String, rank: Rank): PlayerProfile {
        val profile = PlayerService.removeRank(player, rank)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
            RankAttachments.refresh(it)
        }

        return profile
    }

    suspend fun addTag(player: String, tag: String) {
        val profile = PlayerService.addTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun removeTag(player: String, tag: String) {
        val profile = PlayerService.removeTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun setActiveTag(player: String, tag: UUID?) {
        val profile = PlayerService.setActiveTag(player, tag)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun addNote(player: String, content: String, author: SimplePlayer) {
        val profile = PlayerService.addNote(player, content, author)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun removeNote(player: String, id: Int) {
        val profile = PlayerService.removeNote(player, id)

        PlayerManager.getPlayer(profile._id)?.let {
            add(profile)
        }
    }

    suspend fun getPunishmentHistory(player: String): List<Punishment> {
        return PlayerService.getPunishmentHistory(player)
    }

    suspend fun lookup(player: String, includeAlts: Boolean = false): PlayerService.PlayerLookupResponse {
        return PlayerService.lookup(player, includeAlts).also { add(it.player) }
    }

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) = runBlocking {
        val ip = event.address.hostAddress
        val (playerProfile, activeSession, activePunishments) = PlayerService.login(event.uniqueId, event.name, ip)
        if (activeSession == null) { // Player is not allowed to join (banned)
            val ban = activePunishments.find { it.action.isBan() }!!
            val expiryString =
                if (ban.action.isPermanent()) "&7This ban is permanent." else "&7This ban will expire on &f${ban.expiresAt}&7."
            event.kickMessage =
                "&7You have been ${ban.action.kind.pastTense} from the server for &c${ban.reason.name}&7.\n\n&c${ban.reason.message}\n\n$expiryString\n&7Appeal at &bhttps://warzone.network/appeal".color()
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
        } else {
            queuedJoins[event.uniqueId] = Triple(playerProfile, activeSession, activePunishments)
        }
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) = runBlocking {
        val player = event.player
        val ip = event.address.hostAddress

        val (profile, session, activePuns) = queuedJoins[player.uniqueId]
            ?: throw RuntimeException("Queued join unavailable: ${player.name}")

        val context = PlayerManager.createPlayer(player, session, activePuns)

        add(profile)

        RankAttachments.createAttachment(context)
        RankAttachments.refresh(context)
    }

    @EventHandler
    fun onPlayerLogout(event: PlayerQuitEvent) = runBlocking {
        val player = event.player
        val uuid = player.uniqueId

        val activeSession = PlayerManager.getPlayer(uuid)?.activeSession ?: return@runBlocking

        val context = PlayerManager.removePlayer(uuid)!! // We know the player is online
        RankAttachments.removeAttachment(context)

        remove(uuid)

        val sessionLength = Date().time - activeSession.createdAt.time
        PlayerService.logout(uuid, sessionLength)
    }

    override fun getCommands(): List<Any> {
        return listOf(PlayerCommands())
    }
}