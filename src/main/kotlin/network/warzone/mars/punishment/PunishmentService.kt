package network.warzone.mars.punishment

import com.github.kittinunf.result.Result
import kotlinx.serialization.Serializable
import network.warzone.mars.api.exceptions.ApiException
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.Service
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.punishment.exceptions.PunishmentMissingException
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentAction
import network.warzone.mars.punishment.models.PunishmentReason
import network.warzone.mars.punishment.models.PunishmentType
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.mapErrorSmart
import network.warzone.mars.utils.parseHttpException
import java.util.*

object PunishmentService : Service<Punishment>() {
    override suspend fun get(target: String): Result<Punishment, PunishmentMissingException> {
        return parseHttpException {
            apiClient.get<Punishment>("/mc/punishments/$target")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PUNISHMENT_MISSING -> PunishmentMissingException(target)
                else -> TODO()
            }
        }
    }

    suspend fun create(
        reason: PunishmentReason,
        offence: Int,
        action: PunishmentAction,
        note: String?,
        punisher: SimplePlayer,
        targetName: String,
        targetIps: List<String>,
        silent: Boolean
    ): Result<Punishment, PlayerMissingException> {
        return parseHttpException<Punishment> {
            apiClient.post(
                "/mc/players/${targetName}/punishments", PunishmentIssueRequest(
                    reason,
                    offence,
                    action,
                    note,
                    punisher,
                    targetName,
                    targetIps,
                    silent
                )
            )
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> PlayerMissingException(targetName)
                else -> TODO()
            }
        }
    }

    suspend fun listTypes(): List<PunishmentType> {
        return apiClient.get("/mc/punishments/types")
    }

    data class PunishmentIssueRequest(
        val reason: PunishmentReason,
        val offence: Int,
        val action: PunishmentAction,
        val note: String?,
        val punisher: SimplePlayer,
        val targetName: String,
        val targetIps: List<String>,
        val silent: Boolean
    )
}