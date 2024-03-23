package network.warzone.mars.api.socket.models

import network.warzone.mars.match.models.Contribution
import org.bukkit.Material

data class CorePartial(val id: String, val name: String, val ownerName: String, val material: Material)
data class CoreLeakData(val coreId: String, val contributions: List<Contribution>)