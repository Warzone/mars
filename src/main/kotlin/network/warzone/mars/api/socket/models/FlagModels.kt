package network.warzone.mars.api.socket.models

import org.bukkit.ChatColor
import java.util.*

data class FlagPartial(val id: String, val name: String, val colour: ChatColor, val ownerName: String?)
data class FlagPickupData(val flagId: String, val playerId: UUID)
data class FlagCaptureData(val flagId: String, val playerId: UUID, val heldTime: Long)
data class FlagDropData(val flagId: String, val playerId: UUID, val heldTime: Long)
data class FlagDefendData(val flagId: String, val playerId: UUID)