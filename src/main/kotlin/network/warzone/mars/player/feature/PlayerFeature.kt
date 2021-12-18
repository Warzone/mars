package network.warzone.mars.player.feature

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import kotlinx.coroutines.runBlocking
import network.warzone.mars.Mars
import network.warzone.mars.feature.named.NamedCacheFeature
import network.warzone.mars.player.PlayerContext
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.commands.PlayerCommands
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.Session
import network.warzone.mars.punishment.commands.PunishCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.RankAttachments
import network.warzone.mars.rank.exceptions.RankAlreadyPresentException
import network.warzone.mars.rank.exceptions.RankNotPresentException
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.exceptions.TagAlreadyPresentException
import network.warzone.mars.tag.exceptions.TagNotPresentException
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.color
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object PlayerFeature : NamedCacheFeature<PlayerProfile, PlayerService>(), Listener {
    override val service = PlayerService

    val queuedJoins = hashMapOf<UUID, Triple<PlayerProfile, Session, List<Punishment>>>()

    init {
        Mars.registerEvents(this)
    }

    /**
     * Adds a [Rank] to a [PlayerContext].
     *
     * @param player The [PlayerContext] the [Rank] is to be added to.
     * @param rank The [Rank] being to be added to the [PlayerContext].
     *
     * @return A [Result] monad containing the [PlayerContext] and a [RankAlreadyPresentException] if the [PlayerContext] already has the [Rank].
     */
    suspend fun addRank(player: PlayerContext, rank: Rank): Result<PlayerContext, RankAlreadyPresentException> {
        // If the player already has the rank, return an exception.
        if (player.getPlayerProfile().rankIds.contains(rank._id))
            return Result.failure(RankAlreadyPresentException(player, rank))

        // Send request to add the rank.
        service
            .addRankToPlayer(player.uuid, rank._id)
            .failure { return Result.failure(it) }

        // Get the player's profile.
        val profile = player.getPlayerProfile()

        // Add the rank to the player's profile and regenerate the relation.
        profile.rankIds.add(rank._id)
        profile.generate()

        // Refresh the player's permissions.
        RankAttachments.refresh(player)

        // Return the player.
        return Result.success(player)
    }

    /**
     * Removes a [Rank] from a [PlayerContext].
     *
     * @param player The [PlayerContext] the [Rank] is to be removed from.
     * @param rank The [Rank] to be removed from the [PlayerContext].
     *
     * @throws RankNotPresentException If the [PlayerContext] does not have the [Rank].
     *
     * @return A [Result] monad containing the [PlayerContext] and a [RankNotPresentException] if the [PlayerContext] does not have the [Rank].
     */
    suspend fun removeRank(player: PlayerContext, rank: Rank): Result<PlayerContext, RankNotPresentException> {
        // If the player does not have the rank, return an exception.
        if (!player.getPlayerProfile().rankIds.contains(rank._id))
            return Result.failure(RankNotPresentException(player, rank))

        // Send request to remove the rank.
        service
            .removeRankFromPlayer(player.uuid, rank._id)
            .failure { return Result.failure(it) }

        // Get the player's profile.
        val profile = player.getPlayerProfile()

        // Remove the rank from the player's profile and regenerate the relation.
        profile.rankIds.remove(rank._id)
        profile.generate()

        // Refresh the player's permissions.
        RankAttachments.refresh(player)

        // Return the updated player.
        return Result.success(player)
    }

    suspend fun addTag(player: PlayerContext, tag: Tag): Result<PlayerContext, TagAlreadyPresentException> {
        if (player.getPlayerProfile().tagIds.contains(tag._id))
            return Result.failure(TagAlreadyPresentException(player, tag))

        service
            .addTagToPlayer(player.uuid, tag._id)
            .failure { return Result.failure(it) }

        val profile = player.getPlayerProfile()

        profile.tagIds.add(tag._id)
        profile.generate()

        return Result.success(player)
    }

    suspend fun removeTag(player: PlayerContext, tag: Tag): Result<PlayerContext, TagNotPresentException> {
        if (!player.getPlayerProfile().tagIds.contains(tag._id))
            return Result.failure(TagNotPresentException(player, tag))

        service
            .removeTagFromPlayer(player.uuid, tag._id)
            .failure { return Result.failure(it) }

        val profile = player.getPlayerProfile()

        if (profile.activeTagId == tag._id) profile.activeTagId = null

        profile.tagIds.remove(tag._id)
        profile.generate()

        return Result.success(player)
    }

    suspend fun setActiveTag(player: PlayerContext, tag: Tag): Result<PlayerContext, FeatureException> {
        service.setActiveTag(player.uuid, tag._id)

        val profile = player.getPlayerProfile()
        profile.activeTagId = tag._id
        profile.generate()

        return Result.success(player)
    }

    suspend fun removeActiveTag(player: PlayerContext): Result<PlayerContext, PlayerMissingException> {
        service.setActiveTag(player.uuid, null)

        val profile = player.getPlayerProfile()
        profile.activeTagId = null
        profile.generate()

        return Result.success(player)
    }

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) = runBlocking {
        val ip = event.address.hostAddress
        val (playerProfile, activeSession, activePunishments) = service.login(event.uniqueId, event.name, ip)
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
        println(activePuns)

        add(profile.generate())

        RankAttachments.createAttachment(context)
        RankAttachments.refresh(context)
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


    override fun getCommands(): List<Any> {
        return listOf(PlayerCommands())
    }
}