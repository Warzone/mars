package network.warzone.mars.api.socket.models

import network.warzone.mars.match.models.Contribution
import org.bukkit.Material
import java.util.*

data class DestroyablePartial(
    val id: String,
    val name: String,
    val ownerName: String,
    val material: Material,
    val breaksRequired: Int,
    val blockCount: Int
)

data class DestroyableDestroyData(val destroyableId: String, val material: String, val contributions: List<Contribution>)
data class DestroyableDamageData(val destroyableId: String, val material: String, val playerId: UUID, val damage: Int)