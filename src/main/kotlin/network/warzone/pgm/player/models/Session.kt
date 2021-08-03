@file:UseSerializers(UUIDSerializer::class, DateAsLongSerializer::class)
package network.warzone.pgm.player.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import network.warzone.pgm.feature.resource.Resource
import network.warzone.pgm.utils.DateAsLongSerializer
import network.warzone.pgm.utils.UUIDSerializer
import java.util.*

@Serializable
data class Session (
    override val _id: UUID,

    val createdAt: Date,
    val endedAt: Date?,

    val playerId: UUID
) : Resource {
    override fun generate(): Session {
        return this
    }
}