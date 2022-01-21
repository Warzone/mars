package network.warzone.mars.punishment

import com.github.kittinunf.result.Result
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.CachedFeature
import network.warzone.mars.punishment.commands.PunishCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentAction
import network.warzone.mars.punishment.models.PunishmentReason
import network.warzone.mars.punishment.models.PunishmentType
import java.util.*

object PunishmentFeature : CachedFeature<Punishment>() {
    val punishmentTypes: MutableList<PunishmentType> = mutableListOf()

    override suspend fun init() {
        PunishmentService.listTypes().forEach { punishmentTypes.add(it) }
    }

    override suspend fun fetch(target: String): Punishment? {
        return try {
            ApiClient.get<Punishment>("/mc/punishments/$target")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun issue(
        reason: PunishmentReason,
        offence: Int,
        action: PunishmentAction,
        note: String?,
        punisher: SimplePlayer?,
        targetName: String,
        targetIps: List<String>,
        silent: Boolean
    ): Punishment {
        return PunishmentService.create(
            reason,
            offence,
            action,
            note,
            punisher,
            targetName,
            targetIps,
            silent
        )
    }

    suspend fun revert(
        id: UUID,
        reason: String,
        reverter: SimplePlayer
    ): Punishment {
        return PunishmentService.revert(id, reason, reverter)
    }

    override fun getCommands(): List<Any> {
        return listOf(PunishCommands())
    }
}