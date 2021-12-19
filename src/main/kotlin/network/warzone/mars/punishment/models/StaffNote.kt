package network.warzone.mars.punishment.models

import network.warzone.mars.api.socket.models.SimplePlayer
import java.util.*

data class StaffNote(val id: Int, val author: SimplePlayer, val content: String, val createdAt: Date)