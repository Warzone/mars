@file:UseSerializers(UUIDSerializer::class, DateAsLongSerializer::class)

package network.warzone.pgm.player.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import network.warzone.pgm.feature.named.NamedResource
import network.warzone.pgm.feature.relations.Relation
import network.warzone.pgm.feature.resource.ResourceType
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.DateAsLongSerializer
import network.warzone.pgm.utils.UUIDSerializer
import java.util.*

@Serializable
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
    @Transient var tags: List<Any> = emptyList()
) : NamedResource {

    override fun generate(): PlayerProfile {
        ranks = rankIds.map {
            Relation(ResourceType.Rank, it)
        }

        return this
    }

}

