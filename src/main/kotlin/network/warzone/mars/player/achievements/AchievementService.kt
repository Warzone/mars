package network.warzone.mars.player.achievements

import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.onFailure
import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.Agent
import network.warzone.mars.api.ApiClient
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.player.achievements.exceptions.AchievementException
import network.warzone.mars.punishment.PunishmentService
import network.warzone.mars.punishment.models.Punishment
import network.warzone.mars.rank.exceptions.RankConflictException
import network.warzone.mars.utils.parseHttpException

object AchievementService {
    suspend fun create(
        name: String,
        description: String,
        agent: Agent
    ): Achievement {
        val request = parseHttpException {
            ApiClient.post<Achievement, AchievementCreateRequest>(
                "/mc/achievements", AchievementCreateRequest(
                    name,
                    description,
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

    data class AchievementCreateRequest(
        val name: String,
        val description: String,
        val agent: Agent
    )
}