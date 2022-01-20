package network.warzone.mars.rank

import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.NamedCachedFeature
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.rank.commands.RankCommands
import network.warzone.mars.rank.exceptions.RankConflictException
import network.warzone.mars.rank.exceptions.RankMissingException
import network.warzone.mars.rank.models.Rank
import java.util.*

object RankFeature : NamedCachedFeature<Rank>() {
    override suspend fun init() {
        list()
    }

    override suspend fun fetch(target: String): Rank? {
        return try {
            ApiClient.get<Rank>("/mc/ranks/$target")
        } catch (e: Exception) {
            null
        }
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
     * @return The new [Rank] if the operation was successful. Will otherwise throw a `FeatureException`.
     */
    suspend fun create(
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Rank {
        // If a rank by the name already exists, throws an exception.
        if (has(name)) throw RankConflictException(name)

        // Requests the creation of a new rank. Adds the created rank to the cache.
        return RankService.create(
           name = name,
            displayName = displayName ?: name,
            priority = priority ?: 0,
            prefix = prefix,
            permissions = emptyList(),
            staff = staff ?: false,
            applyOnJoin = applyOnJoin ?: false
        ).also { add(it) }
    }

    /**
     * Updates a target [Rank] by applying the properties of a new [Rank] to itself.
     *
     * @param id The UUID of the target [Rank] to be updated.
     * @param newRank The new [Rank] to apply to the target.
     *
     * @return The updated [Rank] if the operation was successful. Will otherwise throw a `FeatureException`.
     */
    suspend fun update(id: UUID, newRank: Rank): Rank {
        // Checks if a rank exists by the name of the new rank, if so throws an exception.
        if (has(newRank.name) && getKnown(newRank.name)._id != id) throw RankConflictException(newRank.name)
        // Checks if the target rank exists, if not throws an exception.
        if (!has(id)) throw RankMissingException(id.toString())

        // Sets the existing resource to the new resource.
        add(newRank)

        // Request for the rank to be updated.
        return RankService.update(
            id = id,
            name = newRank.name,
            displayName = newRank.displayName,
            priority = newRank.priority,
            prefix = newRank.prefix,
            permissions = newRank.permissions,
            staff = newRank.staff,
            applyOnJoin = newRank.applyOnJoin
        )
    }

    /**
     * Deletes a [Rank].
     *
     * @param uuid The [UUID] of the [Rank]
     *
     */
    suspend fun delete(uuid: UUID) {
        // Request the rank to be deleted, throwing an exception if it fails.
        RankService.delete(uuid)

        // If deletion is successful, query all the players with the rank and remove it from them.
        PlayerFeature.query {
            it.rankIds.contains(uuid)
        }.forEach {
            it.rankIds.remove(uuid)

            // Refresh permissions
            RankAttachments.refresh(PlayerManager.getPlayer(it._id)!!)
        }

        // Removes the now deleted rank from the cache.
        remove(uuid)
    }

    suspend fun list(): List<Rank> {
        return RankService.list().onEach { add(it) }
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