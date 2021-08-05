@file:UseSerializers(UUIDSerializer::class, DateAsLongSerializer::class)
package network.warzone.pgm.ranks.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import network.warzone.pgm.feature.named.NamedResource
import network.warzone.pgm.feature.resource.Resource
import network.warzone.pgm.utils.DateAsLongSerializer
import network.warzone.pgm.utils.UUIDSerializer
import java.util.*

@Serializable
class Rank (
    override val _id: UUID,
    override var name: String,
    var nameLower: String,
    var displayName: String?,
    var prefix: String?,
    var priority: Int,
    var createdAt: Long,
    var staff: Boolean,
    var applyOnJoin: Boolean,
    val permissions: MutableList<String>
) : NamedResource {
    override fun generate(): Resource {
        TODO("Not yet implemented")
    }
}