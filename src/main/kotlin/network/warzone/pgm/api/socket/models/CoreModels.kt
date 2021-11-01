package network.warzone.pgm.api.socket.models

import network.warzone.pgm.match.models.Contribution
import org.bukkit.Material
import java.util.*

data class CorePartial( val id: String, val name: String, val ownerName: String, val material: Material)
data class CoreDamageData(val coreId: String, val playerId: UUID)
data class CoreLeakData(val coreId: String, val contributions: List<Contribution>)