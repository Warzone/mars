package network.warzone.mars.player.models

import network.warzone.mars.feature.named.NamedResource
import network.warzone.mars.feature.relations.Relation
import network.warzone.mars.feature.resource.ResourceType
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
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

    var activeTagId: UUID?,
    @Transient var activeTag: Relation<Tag>? = null,

    val tagIds: MutableList<UUID>,
    @Transient var tags: List<Relation<Tag>> = emptyList()
) : NamedResource {

    suspend fun tags(): List<Tag> = tags.map { it.get() }
    suspend fun ranks(): List<Rank> = ranks.map { it.get() }
    suspend fun activeTag(): Tag? = activeTag?.get()

    override fun generate(): PlayerProfile {
        ranks = rankIds.map {
            Relation(ResourceType.Rank, it)
        }

        tags = tagIds.map {
            Relation(ResourceType.Tag, it)
        }

        activeTag = null
        activeTagId?.let {
            activeTag = Relation(ResourceType.Tag, it)
        }

        return this
    }

}

