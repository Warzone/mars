package network.warzone.mars.tag

import com.github.kittinunf.result.Result
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.feature.Service
import network.warzone.mars.tag.exceptions.TagConflictException
import network.warzone.mars.tag.exceptions.TagMissingException
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.mapErrorSmart
import network.warzone.mars.utils.parseHttpException
import java.util.*

object TagService : Service<Tag>() {

    suspend fun create(name: String, display: String): Result<Tag, TagConflictException> {
        return parseHttpException<Tag> {
            apiClient.post("/mc/tags", TagDataRequest(name, display))
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_ALREADY_PRESENT -> TagConflictException(name)
                else -> TODO()
            }
        }
    }

    suspend fun delete(uuid: UUID): Result<Unit, TagMissingException> {
        return parseHttpException {
            apiClient.delete<Unit>("/mc/tags/$uuid")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_MISSING -> TagMissingException(uuid.toString())
                else -> TODO()
            }
        }
    }

    suspend fun update(
        id: UUID,
        name: String,
        display: String
    ): Result<Unit, FeatureException> {
        return parseHttpException {
            apiClient.put<Unit, TagDataRequest>("/mc/tags/${id}", TagDataRequest(
                name,
                display
            ))
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_MISSING -> TagMissingException(name)
                ApiExceptionType.TAG_CONFLICT -> TagConflictException(name)
                else -> TODO()
            }
        }
    }

    suspend fun list(): List<Tag> {
        return apiClient.get("/mc/tags")
    }

    override suspend fun get(target: String): Result<Tag, TagMissingException> {
        return parseHttpException<Tag> {
            apiClient.get("mc/tags/$target")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.TAG_MISSING -> TagMissingException(target)
                else -> TODO()
            }
        }
    }

    data class TagDataRequest(val name: String, val display: String)

}