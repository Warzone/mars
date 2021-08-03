package network.warzone.pgm.ranks

import kotlinx.serialization.Serializable
import network.warzone.pgm.api.ErrorCode
import network.warzone.pgm.api.http.Response
import network.warzone.pgm.feature.Service
import network.warzone.pgm.ranks.models.Rank
import network.warzone.pgm.utils.except
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
    ): Rank? {
        return apiClient.post<RankCreateResponse, RankCreateRequest>("/mc/ranks", RankCreateRequest(
            name,
            displayName,
            priority,
            prefix,
            permissions,
            staff,
            applyOnJoin
        )).except().rank
    }

    suspend fun delete(uuid: UUID) {
        apiClient.delete<Response>("/mc/ranks/$uuid").except()
    }

    suspend fun list(): List<Rank> {
        return apiClient.get<RankListResponse>("/mc/ranks").ranks
    }

    override suspend fun get(target: String): Rank {
        return apiClient.get("/mc/ranks/$target")
    }

    @Serializable
    data class RankCreateRequest(
        val name: String,
        val displayName: String?,
        val priority: Int?,
        val prefix: String?,
        val permissions: List<String>?,
        val staff: Boolean?,
        val applyOnJoin: Boolean?
    )
    @Serializable
    data class RankCreateResponse(
        val rank: Rank? = null,

        override val code: ErrorCode? = null,
        override val message: String? = null,
        override val error: Boolean   = false
    ) : Response()

    @Serializable
    data class RankListResponse(
        val ranks: List<Rank>,

        override val code: ErrorCode? = null,
        override val message: String? = null,
        override val error: Boolean   = false
    ) : Response()

}