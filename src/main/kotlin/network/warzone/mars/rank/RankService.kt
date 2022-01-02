package network.warzone.mars.rank

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.isFailure
import com.github.kittinunf.result.onFailure
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.rank.exceptions.RankConflictException
import network.warzone.mars.rank.exceptions.RankMissingException
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.utils.parseHttpException
import java.util.*

object RankService {
    suspend fun create(
        name: String,
        displayName: String?,
        priority: Int?,
        prefix: String?,
        permissions: List<String>?,
        staff: Boolean?,
        applyOnJoin: Boolean?
    ): Rank {
        val request =
            parseHttpException {
                ApiClient.post<Rank, RankDataRequest>(
                    "/mc/ranks",
                    RankDataRequest(name, displayName, priority, prefix, permissions, staff, applyOnJoin)
                )
            }
        val rank = request.getOrNull()
        if (rank != null) return rank

        request.onFailure {
            when (it.code) {
                ApiExceptionType.RANK_CONFLICT -> throw RankConflictException(name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
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
    ): Rank {
        val request =
            parseHttpException {
                ApiClient.put<Rank, RankDataRequest>(
                    "/mc/ranks/$id",
                    RankDataRequest(name, displayName, priority, prefix, permissions, staff, applyOnJoin)
                )
            }
        val rank = request.getOrNull()
        if (rank != null) return rank

        request.onFailure {
            when (it.code) {
                ApiExceptionType.RANK_CONFLICT -> throw RankConflictException(name)
                ApiExceptionType.RANK_MISSING -> throw RankMissingException(name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun delete(id: UUID) {
        val request = parseHttpException { ApiClient.delete<Unit>("/mc/tags/$id") }

        request.onFailure {
            when (it.code) {
                ApiExceptionType.RANK_MISSING -> throw RankMissingException(id.toString())
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }
    }

    suspend fun list(): List<Rank> {
        return ApiClient.get("/mc/ranks")
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