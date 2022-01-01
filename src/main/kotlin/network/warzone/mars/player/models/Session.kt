package network.warzone.mars.player.models

import network.warzone.mars.feature.resource.Resource
import java.util.*

data class Session(
    override val _id: UUID,

    val createdAt: Date,
    val endedAt: Date?,

    val playerId: UUID
) : Resource {
    override fun generate(): Session {
        return this
    }
}