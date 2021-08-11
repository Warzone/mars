package network.warzone.pgm.player.models

import network.warzone.pgm.feature.named.NamedResource
import network.warzone.pgm.feature.relations.Relation
import network.warzone.pgm.feature.resource.ResourceType
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.tags.models.Tag
import java.util.*

data class PlayerProfile(
    override val _id: UUID,

    override val name: String,
    val nameLower: String,

    val firstJoinedAt: Date,
    val lastJoinedAt: Date,

    val playtime: Long,

    val ips: List<String>,

    val rankIds: MutableList<UUID>,
    @Transient var ranks: List<Relation<Rank>> = emptyList(),

    val tagIds: MutableList<UUID>,
    @Transient var tags: List<Relation<Tag>> = emptyList()
) : NamedResource {

    suspend fun tags(): List<Tag> = tags.map { it.get() }
    suspend fun ranks(): List<Rank> = ranks.map { it.get() }

    override fun generate(): PlayerProfile {
        ranks = rankIds.map {
            Relation(ResourceType.Rank, it)
        }

        tags = tagIds.map {
            Relation(ResourceType.Tag, it)
        }

        return this
    }

}

