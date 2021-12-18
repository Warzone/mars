package network.warzone.mars.punishment.models

import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.resource.Resource
import java.util.*

data class Punishment(
    override val _id: UUID,
    val reason: PunishmentReason,
    val issuedAt: Date,
    val silent: Boolean,
    val offence: Int,
    val action: PunishmentAction,
    val note: String?,
    val punisher: SimplePlayer,
    val target: SimplePlayer,
    val targetIps: List<String>,
    val reversion: PunishmentReversion? = null
) : Resource {
    override fun generate(): Resource {
        return this
    }

    val expiresAt: Date
        get() {
            return if (action.isPermanent()) Date(-1L) else Date(issuedAt.time + action.length)
        }

    val isActive: Boolean
        get() {
            if (reversion != null) return false
            return action.isPermanent() || Date().time < expiresAt.time
        }

    val isReverted: Boolean
    get() {
        return reversion != null
    }
}

data class PunishmentReason(val name: String, val message: String, val short: String)

data class PunishmentReversion(val revertedAt: Long, val reverter: SimplePlayer, val reason: String)