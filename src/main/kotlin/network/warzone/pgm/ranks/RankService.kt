package network.warzone.pgm.ranks

import com.github.kittinunf.result.Result
import network.warzone.pgm.api.http.ApiExceptionType
import network.warzone.pgm.feature.Service
import network.warzone.pgm.ranks.exceptions.RankConflictException
import network.warzone.pgm.ranks.exceptions.RankMissingException
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.FeatureException
import network.warzone.pgm.utils.mapErrorSmart
import network.warzone.pgm.utils.parseHttpException
import java.util.*

object RankService : Service<Rank>() {

    suspend fun create(
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        permissions: List<String>?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Result<Rank, RankConflictException> {
        return parseHttpException {
            apiClient.post<Rank, RankDataRequest>("/mc/ranks", RankDataRequest(
                name,
                displayName,
                priority,
                prefix,
                permissions,
                staff,
                applyOnJoin
            ))
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_CONFLICT -> RankConflictException(name)
                else -> TODO()
            }
        }
    }

    suspend fun update(
        id: UUID,
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        permissions: List<String>?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Result<Unit, FeatureException> {
        return parseHttpException {
            apiClient.put<Unit, RankDataRequest>("/mc/ranks/$id", RankDataRequest(
                name,
                displayName,
                priority,
                prefix,
                permissions,
                staff,
                applyOnJoin
            ))
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_CONFLICT -> RankConflictException(name)
                ApiExceptionType.RANK_MISSING -> RankMissingException(name)
                else -> TODO()
            }
        }
    }

    suspend fun delete(uuid: UUID): Result<Unit, RankMissingException> {
        return parseHttpException {
            apiClient.delete<Unit>("/mc/ranks/$uuid")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_MISSING -> RankMissingException(uuid.toString())
                else -> TODO()
            }
        }
    }

    suspend fun list(): List<Rank> {
        return apiClient.get("/mc/ranks")
    }

    override suspend fun get(target: String): Result<Rank, RankMissingException> {
        return parseHttpException {
            apiClient.get<Rank>("/mc/ranks/$target")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.RANK_MISSING -> RankMissingException(target)
                else -> TODO()
            }
        }
    }

    data class RankDataRequest(
        val name: String,
        val displayName: String?,
        val priority: Int?,
        val prefix: String?,
        val permissions: List<String>?,
        val staff: Boolean?,
        val applyOnJoin: Boolean?
    )

}