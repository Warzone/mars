package network.warzone.pgm.ranks

import network.warzone.pgm.api.exceptions.ApiException
import network.warzone.pgm.feature.named.NamedCacheFeature
import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.ranks.commands.RankCommands
import network.warzone.pgm.ranks.models.Rank
import java.util.*

object RankFeature : NamedCacheFeature<Rank, RankService>() {
    override val service = RankService

    override suspend fun init() {
        list()
    }

    suspend fun createRank(
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Rank? {
        return service
            .create(
                name = name,
                displayName = displayName ?: name,
                priority = priority ?: 0,
                prefix = prefix,
                permissions = emptyList(),
                staff = staff ?: false,
                applyOnJoin = applyOnJoin ?: false
            )
            .also { it?.let { add(it) } }
    }

    suspend fun list(): List<Rank> {
        return service.list()
            .also(::sync)
    }

    suspend fun deleteRank(name: String) {
        val rank = get(name)

        deleteRank(rank._id)
    }

    suspend fun deleteRank(uuid: UUID) {
        try {
            service.delete(uuid)

            // If deletion is successful, we'll reach this:
            PlayerFeature.query {
                it.rankIds.contains(uuid)
            }.forEach {
                it.rankIds.remove(uuid)

                // Regenerate relation
                it.generate()
            }

            invalidate(uuid)
        } catch (e: ApiException) {
            throw e
        }
    }

    override fun getCommands(): List<Any> {
        return listOf(RankCommands())
    }
}