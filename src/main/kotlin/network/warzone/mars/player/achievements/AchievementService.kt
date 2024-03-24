package network.warzone.mars.player.achievements

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.Agent
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.player.achievements.exceptions.AchievementException
import network.warzone.mars.player.achievements.exceptions.AchievementMissingException
import network.warzone.mars.player.achievements.models.AchievementCategory
import network.warzone.mars.utils.parseHttpException
import java.util.*

object AchievementService {
    suspend fun create(
        name: String,
        description: String,
        category: AchievementCategory,
        agent: Agent
    ): Achievement {
        val request = parseHttpException {
            ApiClient.post<Achievement, AchievementCreateRequest>(
                "/mc/achievements", AchievementCreateRequest(
                    name,
                    description,
                    category,
                    agent
                )
            )
        }
        val achievement = request.getOrNull()
        if (achievement != null) return achievement

        request.onFailure {
            when (it.code) {
                // Create a custom
                ApiExceptionType.ACHIEVEMENT_CONFLICT -> throw AchievementException()
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }

        throw RuntimeException("Unreachable")
    }

    suspend fun list(): List<Achievement> {
        return ApiClient.get("/mc/achievements")
    }

    suspend fun delete(id: UUID) {
        val request = parseHttpException { ApiClient.delete<Unit>("/mc/achievements/$id") }

        request.onFailure {
            when (it.code) {
                ApiExceptionType.ACHIEVEMENT_MISSING -> throw AchievementMissingException(id.toString())
                else -> TODO("Unexpected API exception: ${it.code}")
            }
        }
    }

    data class AchievementCreateRequest(
        val name: String,
        val description: String,
        val category: AchievementCategory,
        val agent: Agent
    )
}