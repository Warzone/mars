package network.warzone.mars.rank

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import com.github.kittinunf.result.map
import network.warzone.mars.feature.named.NamedCacheFeature
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.rank.commands.RankCommands
import network.warzone.mars.rank.exceptions.RankConflictException
import network.warzone.mars.rank.exceptions.RankMissingException
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.FeatureException
import java.util.*

object RankFeature : NamedCacheFeature<Rank, RankService>() {
    override val service = RankService

    override suspend fun init() {
        list()
    }

    /**
     * Creates a new [Rank].
     *
     * @param name The name of the [Rank].
     * @param displayName Nullable display name for the [Rank].
     * @param priority Nullable priority for the [Rank].
     * @param prefix Nullable prefix for the [Rank].
     * @param staff If the [Rank] is a staff [Rank].
     * @param applyOnJoin If the [Rank] is a default [Rank].
     *
     * @return A [Result] monad containing the new [Rank] and a [RankConflictException] if a rank by the same name alreay exists.
     */
    suspend fun createRank(
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Result<Rank, RankConflictException> {
        // If a rank by the name already exists, returns an exception.
        if (has(name)) return Result.failure(RankConflictException(name))

        // Requests the creation of a new rank. Adds the created rank to the cache.
        return service.create(
                name = name,
                displayName = displayName ?: name,
                priority = priority ?: 0,
                prefix = prefix,
                permissions = emptyList(),
                staff = staff ?: false,
                applyOnJoin = applyOnJoin ?: false
        ).map { add(it) }
    }

    /**
     * Updates a target [Rank] by applying the properties of a new [Rank] to itself.
     *
     * @param id The UUID of the target [Rank] to be updated.
     * @param newRank The new [Rank] to apply to the target.
     *
     * @return A [Result] monad containing the updated [Rank] and
     * - a [RankConflictException] if a [Rank] already exists by the name of the new [Rank].
     * - a [RankMissingException] if the [Rank] does not exist.
     */
    suspend fun updateRank(id: UUID, newRank: Rank): Result<Rank, FeatureException> {
        // Checks if a rank exists by the name of the new rank, if so returns an exception.
        if (has(newRank.name) && getKnown(newRank.name)._id != id) return Result.failure(RankConflictException(newRank.name))
        // Checks if the target rank exists, if not returns an exception.
        if (!has(id)) return Result.failure(RankMissingException(id.toString()))

        // Sets the existing resource to the new resource.
        set(id, newRank)

        // Request for the rank to be updated.
        return service.update(
            id = id,
            name = newRank.name,
            displayName = newRank.displayName,
            priority = newRank.priority,
            prefix = newRank.prefix,
            permissions = newRank.permissions,
            staff = newRank.staff,
            applyOnJoin = newRank.applyOnJoin
        ).map { newRank }
    }

    /**
     * Deletes a [Rank].
     *
     * @param uuid The [UUID] of the [Rank]
     *
     * @return A [Result] monad with a Unit result and a [RankMissingException] if the rank doesn't exist.
     */
    suspend fun deleteRank(uuid: UUID): Result<Unit, RankMissingException> {
        // Request the rank to be deleted, returning an exception if it fails.
        service
            .delete(uuid)
            .failure { return Result.failure(it) }

        // If deletion is successful, query all the players with the rank and remove it from them.
        PlayerFeature.query {
            it.rankIds.contains(uuid)
        }.forEach {
            it.rankIds.remove(uuid)

            // Regenerate relation
            it.generate()

            // Refresh permissions
            RankAttachments.refresh(PlayerManager.getPlayer(it._id)!!)
        }

        // Removes the now deleted rank from the cache.
        invalidate(uuid)

        return Result.success(Unit)
    }

    suspend fun list(): List<Rank> {
        return service.list()
            .also(::sync)
    }

    suspend fun updatePermissions(rank: Rank) {
        PlayerFeature.query {
            it.rankIds.contains(rank._id)
        }.forEach {
            RankAttachments.refresh(PlayerManager.getPlayer(it._id)!!)
        }
    }

    override fun getSubcommands(): Map<List<String>, Any> {
        return mapOf(
            listOf("rank", "ranks") to RankCommands()
        )
    }
}