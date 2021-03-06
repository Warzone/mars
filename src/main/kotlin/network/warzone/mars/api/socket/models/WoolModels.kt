package network.warzone.mars.api.socket.models

import org.bukkit.ChatColor
import java.util.*

data class WoolPartial(val id: String, val name: String, val ownerName: String?, val color: ChatColor)
data class WoolData(val woolId: String, val playerId: UUID)
data class WoolDropData(val woolId: String, val playerId: UUID, val heldTime: Long)
