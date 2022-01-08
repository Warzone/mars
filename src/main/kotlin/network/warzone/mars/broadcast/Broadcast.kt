package network.warzone.mars.broadcast

import network.warzone.mars.feature.Resource
import java.util.*

/**
 * Broadcasts don't have UUIDs so we just create random UUID
 */
data class Broadcast(
    override val _id: UUID = UUID.randomUUID(),
    val name: String,
    val message: String,
    val permission: String? = null,
    val newline: Boolean = true
) : Resource