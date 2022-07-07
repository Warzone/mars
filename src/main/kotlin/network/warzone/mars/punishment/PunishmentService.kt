package network.warzone.mars.punishment

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.punishment.exceptions.PunishmentAlreadyRevertedException
import network.warzone.mars.punishment.exceptions.PunishmentMissingException
import network.warzone.mars.punishment.models.PlayerPunishmentProtectionRequest
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.punishment.models.PunishmentAction
import network.warzone.mars.punishment.models.PunishmentReason
import network.warzone.mars.punishment.models.PunishmentType
import network.warzone.mars.utils.parseHttpException
import java.util.*

object PunishmentService {

    suspend fun isProtected(
        player: SimplePlayer
    ): Boolean {

        val request = parseHttpException {
            ApiClient.get<Boolean>("/mc/players/$player/punishmentProtection")
        }

        val status = request.getOrNull()
        if (status != null) return status

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player.name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun protect(
        player: SimplePlayer
    ): Boolean {

        val request = parseHttpException {
            ApiClient.post<Boolean, PlayerPunishmentProtectionRequest>(
                "/mc/players/$player/punishmentProtection",
                PlayerPunishmentProtectionRequest(player, true)
            )
        }

        val status = request.getOrNull()
        if (status != null) return status

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(player.name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun create(
        reason: PunishmentReason,
        offence: Int,
        action: PunishmentAction,
        note: String?,
        punisher: SimplePlayer?,
        targetName: String,
        targetIps: List<String>,
        silent: Boolean
    ): Punishment {
        val request = parseHttpException {
            ApiClient.post<Punishment, PunishmentIssueRequest>(
                "/mc/players/$targetName/punishments", PunishmentIssueRequest(
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
        }
        val punishment = request.getOrNull()
        if (punishment != null) return punishment

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> throw PlayerMissingException(targetName)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun revert(
        id: UUID,
        reason: String,
        reverter: SimplePlayer
    ): Punishment {
        val request = parseHttpException {
            ApiClient.post<Punishment, PunishmentRevertRequest>(
                "/mc/punishments/$id/revert",
                PunishmentRevertRequest(reason, reverter)
            )
        }
        val punishment = request.getOrNull()
        if (punishment != null) return punishment

        request.onFailure {
            when (it.code) {
                ApiExceptionType.PUNISHMENT_MISSING -> throw PunishmentMissingException(id.toString())
                ApiExceptionType.PUNISHMENT_ALREADY_REVERTED -> throw PunishmentAlreadyRevertedException(id.toString())
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun listTypes(): List<PunishmentType> {
        return ApiClient.get("/mc/punishments/types")
    }

    data class PunishmentIssueRequest(
        val reason: PunishmentReason,
        val offence: Int,
        val action: PunishmentAction,
        val note: String?,
        val punisher: SimplePlayer?,
        val targetName: String,
        val targetIps: List<String>,
        val silent: Boolean
    )

    data class PunishmentRevertRequest(val reason: String, val reverter: SimplePlayer)
}
