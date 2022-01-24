package network.warzone.mars.player.models

import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.Resource
import java.util.*

data class Session(
    override val _id: UUID,
    val serverId: String,

    val createdAt: Date,
    val endedAt: Date?,

    val ip: String,

    val player: SimplePlayer
) : Resource