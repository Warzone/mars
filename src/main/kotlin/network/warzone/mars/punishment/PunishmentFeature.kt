package network.warzone.mars.punishment

import com.github.kittinunf.result.Result
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.Feature
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.punishment.commands.PunishCommands
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentAction
import network.warzone.mars.punishment.models.PunishmentReason
import network.warzone.mars.punishment.models.PunishmentType
import network.warzone.mars.utils.FeatureException
import java.util.*

object PunishmentFeature : Feature<Punishment, PunishmentService>() {
    override val service = PunishmentService
    override suspend fun get(uuid: UUID): Result<Punishment, FeatureException> {
        return service.get(uuid.toString())
    }

    override fun getCached(uuid: UUID): Punishment? {
        throw NotImplementedError("PunishmentFeature has no cache")
    }

    val punishmentTypes: MutableList<PunishmentType> = mutableListOf()

    override suspend fun init() {
        service.listTypes().forEach { punishmentTypes.add(it) }
    }

    suspend fun issuePunishment(
        reason: PunishmentReason,
        offence: Int,
        action: PunishmentAction,
        note: String?,
        punisher: SimplePlayer,
        targetName: String,
        targetIps: List<String>,
        silent: Boolean
    ): Result<Punishment, PlayerMissingException> {
        return service.create(
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

    suspend fun revertPunishment(punishment: UUID, reason: String, reverter: SimplePlayer): Result<Punishment, FeatureException> {
        return service.revert(punishment, reason, reverter)
    }

    override fun getCommands(): List<Any> {
        return listOf(PunishCommands())
    }
}