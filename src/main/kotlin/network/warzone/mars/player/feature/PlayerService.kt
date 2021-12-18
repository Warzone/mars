package network.warzone.mars.player.feature

import com.github.kittinunf.result.Result
import network.warzone.mars.api.exceptions.ApiException
import network.warzone.mars.api.http.ApiExceptionResponse
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.feature.Service
import network.warzone.mars.player.PlayerManager
import network.warzone.mars.player.feature.exceptions.PlayerMissingException
import network.warzone.mars.player.models.PlayerProfile
import network.warzone.mars.player.models.Session
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.rank.exceptions.RankAlreadyPresentException
import network.warzone.mars.rank.exceptions.RankNotPresentException
import network.warzone.mars.tag.TagFeature
import network.warzone.mars.tag.exceptions.TagAlreadyPresentException
import network.warzone.mars.tag.exceptions.TagNotPresentException
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.mapErrorSmart
import network.warzone.mars.utils.parseHttpException
import java.util.*

object PlayerService : Service<PlayerProfile>() {

    suspend fun login(playerId: UUID, playerName: String, ip: String): PlayerLoginResponse {
        return apiClient.post(
            "/mc/players/login", PlayerLoginRequest(
                playerId,
                playerName,
                ip
            )
        )
    }

    suspend fun logout(playerId: UUID, playtime: Long) {
        apiClient.post<ApiExceptionResponse, PlayerLogoutRequest>(
            "/mc/players/logout", PlayerLogoutRequest(
                playerId,
                playtime
            )
        )
    }

    suspend fun lookupPlayer(target: String): Result<PlayerLookupResponse, PlayerMissingException> {
        return parseHttpException<PlayerLookupResponse> { apiClient.get("/mc/players/$target/lookup") }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> PlayerMissingException(target)
                else -> TODO()
            }
        }
    }

    suspend fun addRankToPlayer(playerId: UUID, rankId: UUID): Result<Unit, RankAlreadyPresentException> {
        return parseHttpException {
            apiClient.put<Unit>("/mc/players/$playerId/ranks/$rankId")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_ALREADY_PRESENT -> RankAlreadyPresentException(
                    PlayerManager.getPlayer(playerId)!!,
                    RankFeature.getKnown(rankId)
                )
                else -> TODO()
            }
        }
    }

    suspend fun removeRankFromPlayer(playerId: UUID, rankId: UUID): Result<Unit, RankNotPresentException> {
        return parseHttpException {
            apiClient.delete<Unit>("/mc/players/$playerId/ranks/$rankId")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_NOT_PRESENT -> RankNotPresentException(
                    PlayerManager.getPlayer(playerId)!!,
                    RankFeature.getKnown(rankId)
                )
                else -> TODO()
            }
        }
    }

    suspend fun addTagToPlayer(playerId: UUID, tagId: UUID): Result<Unit, TagAlreadyPresentException> {
        return parseHttpException {
            apiClient.put<Unit>("/mc/players/$playerId/tags/$tagId")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_ALREADY_PRESENT -> TagAlreadyPresentException(
                    PlayerManager.getPlayer(playerId)!!,
                    TagFeature.getKnown(tagId)
                )
                else -> TODO()
            }
        }
    }

    suspend fun removeTagFromPlayer(playerId: UUID, tagId: UUID): Result<Unit, TagNotPresentException> {
        return parseHttpException {
            apiClient.delete<Unit>("/mc/players/$playerId/tags/$tagId")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_NOT_PRESENT -> TagNotPresentException(
                    PlayerManager.getPlayer(playerId)!!,
                    TagFeature.getKnown(tagId)
                )
                else -> TODO()
            }
        }
    }

    suspend fun setActiveTag(playerId: UUID, tagId: UUID?): Result<Unit, FeatureException> {
        return parseHttpException<Unit> {
            apiClient.put("/mc/players/$playerId/active_tag", PlayerActiveTagRequest(tagId))
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> PlayerMissingException(playerId.toString())
                ApiExceptionType.TAG_NOT_PRESENT -> TagNotPresentException(
                    PlayerManager.getPlayer(playerId)!!,
                    TagFeature.getKnown(tagId!!)
                )
                else -> TODO()
            }
        }
    }

    suspend fun getPunishments(target: String): Result<List<Punishment>, PlayerMissingException> {
        return parseHttpException<List<Punishment>> { apiClient.get("/mc/players/$target/punishments") }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> PlayerMissingException(target)
                else -> TODO()
            }
        }
    }

    override suspend fun get(target: String): Result<PlayerProfile, PlayerMissingException> {
        return parseHttpException {
            apiClient.get<PlayerProfile>("/mc/players/$target")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.PLAYER_MISSING -> PlayerMissingException(target)
                else -> TODO()
            }
        }
    }

    data class PlayerLoginRequest(val playerId: UUID, val playerName: String, val ip: String)

    data class PlayerLoginResponse(
        val player: PlayerProfile,
        val activeSession: Session?,
        val activePunishments: List<Punishment>
    )

    data class PlayerLogoutRequest(val playerId: UUID, val playtime: Long)

    data class PlayerActiveTagRequest(val activeTagId: UUID?)

    data class PlayerLookupResponse(val player: PlayerProfile, val alts: List<PlayerAltResponse>)

    data class PlayerAltResponse(val player: PlayerProfile, val punishments: List<Punishment>)
}