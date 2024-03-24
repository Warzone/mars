package network.warzone.mars.tag

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.tag.exceptions.TagConflictException
import network.warzone.mars.tag.exceptions.TagMissingException
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.parseHttpException
import java.util.*

object TagService {
    suspend fun create(name: String, display: String): Tag {
        val request =
            parseHttpException { ApiClient.post<Tag, TagDataRequest>("/mc/tags", TagDataRequest(name, display)) }
        val tag = request.getOrNull()
        if (tag != null) return tag

        request.onFailure {
            when (it.code) {
                ApiExceptionType.TAG_CONFLICT -> throw TagConflictException(name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun delete(id: UUID) {
        val request = parseHttpException { ApiClient.delete<Unit>("/mc/tags/$id") }

        request.onFailure {
            when (it.code) {
                ApiExceptionType.TAG_MISSING -> throw TagMissingException(id.toString())
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }
    }

    suspend fun update(
        id: UUID,
        name: String,
        display: String
    ): Tag {
        val request =
            parseHttpException { ApiClient.put<Tag, TagDataRequest>("/mc/tags/$id", TagDataRequest(name, display)) }
        val tag = request.getOrNull()
        if (tag != null) return tag

        request.onFailure {
            when (it.code) {
                ApiExceptionType.TAG_MISSING -> throw TagMissingException(name)
                ApiExceptionType.TAG_CONFLICT -> throw TagConflictException(name)
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun list(): List<Tag> {
        return ApiClient.get("/mc/tags")
    }

    data class TagDataRequest(val name: String, val display: String)
}