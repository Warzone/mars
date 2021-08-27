package network.warzone.pgm.api.socket.models

import network.warzone.pgm.match.models.Contribution
import org.bukkit.Material
import java.util.*

data class DestroyablePartial(val id: String, val name: String, val ownerName: String, val material: Material, val blockCount: Int )
data class DestroyableDamageData( val destroyableId: String, val playerId: UUID)
data class DestroyableDestroyData( val destroyableId: String, val contributions: List<Contribution> )

