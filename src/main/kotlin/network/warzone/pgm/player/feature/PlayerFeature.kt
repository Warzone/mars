package network.warzone.pgm.player.feature

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import kotlinx.coroutines.runBlocking
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.feature.named.NamedCacheFeature
import network.warzone.pgm.player.PlayerContext
import network.warzone.pgm.player.PlayerManager
import network.warzone.pgm.player.models.PlayerProfile
import network.warzone.pgm.ranks.RankAttachments
import network.warzone.pgm.ranks.exceptions.RankAlreadyPresentException
import network.warzone.pgm.ranks.exceptions.RankNotPresentException
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.tags.exceptions.TagAlreadyPresentException
import network.warzone.pgm.tags.exceptions.TagNotPresentException
import network.warzone.pgm.tags.models.Tag
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

        profile.tagIds.remove(tag._id)
        profile.generate()

        return Result.success(player)
    }

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) = runBlocking {
        val player = event.player
        val ip = event.address.hostAddress

        val (playerProfile, activeSession) = service.login(player.uniqueId, player.name, ip)
        val context = PlayerManager.createPlayer(player, activeSession)

        add(playerProfile.generate())

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

}