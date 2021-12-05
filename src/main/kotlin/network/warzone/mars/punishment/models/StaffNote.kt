package network.warzone.mars.punishment.models

import network.warzone.mars.api.socket.models.SimplePlayer

data class StaffNote(val author: SimplePlayer, val content: String, val createdAt: Long)