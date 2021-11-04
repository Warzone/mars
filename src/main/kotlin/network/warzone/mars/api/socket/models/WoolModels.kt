package network.warzone.mars.api.socket.models

import org.bukkit.ChatColor
import java.util.*

data class WoolPartial(val id: String, val name: String, val ownerName: String?, val colour: ChatColor)
data class WoolData(val woolId: String, val playerId: UUID)
