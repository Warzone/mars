package network.warzone.mars.punishment.models

import kotlinx.serialization.Serializable

@Serializable
data class PunishmentType(
    val name: String,
    val short: String,
    val message: String,
    val actions: List<PunishmentAction>,
    val material: String,
    val position: Int,
    val tip: String? = null,
    val requiredPermission: String? = "mars.punish"
) {
    fun getActionByOffence(offence: Int): PunishmentAction {
        val offenceTooHigh = offence > actions.count()
        val index = if (offenceTooHigh) actions.count() - 1 else offence - 1
        return actions[index]
    }

    fun toReason(): PunishmentReason {
        return PunishmentReason(name, message, short)
    }
}