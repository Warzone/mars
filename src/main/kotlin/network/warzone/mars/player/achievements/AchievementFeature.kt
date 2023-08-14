package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.api.database.models.Agent
import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.CachedFeature
import network.warzone.mars.player.achievements.models.AchievementParent
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.utils.capitalizeFirst
import java.util.*

object AchievementFeature : CachedFeature<Achievement>() {
    override suspend fun init() {
        list()
    }

    override suspend fun fetch(target: String): Achievement? {
        return try {
            ApiClient.get("/mc/achievements/$target")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a new [Achievement].
     *
     * @param name The name of the [Achievement].
     * @param description The description of the [Achievement].
     * @param agent The [Agent] that listens for events and emits completions for the [Achievement].
     *
     * @return The new [Achievement] if the operation was successful. Will otherwise throw a `FeatureException`.
     */
    suspend fun create(
        name: String,
        description: String,
        parent: AchievementParent,
        agent: Agent
    ): Achievement {
        // Requests the creation of a new achievement. Adds the created achievement to the cache.
        return AchievementService.create(
            name = name,
            description = description,
            parent = parent,
            agent = agent
        ).also { AchievementFeature.add(it) }
    }

    /**
     * Deletes an [Achievement].
     *
     * @param uuid The [UUID] of the [Achievement]
     *
     */
    suspend fun delete(uuid: UUID) {
        // Request the achievement to be deleted, throwing an exception if it fails.
        AchievementService.delete(uuid)

        // If deletion is successful, query all the players with the achievement and remove it from them.
        PlayerFeature.query {
            it.stats.achievements.contains(uuid.toString())
        }.forEach {
            it.stats.achievements.remove(uuid.toString())
        }

        // Removes the now deleted achievement from the cache.
        AchievementFeature.remove(uuid)
    }

    suspend fun list(): List<Achievement> {
        val achievements = AchievementService.list()
        if (achievements.isNotEmpty()) AchievementFeature.clear()
        return achievements.onEach { AchievementFeature.add(it) }

    }

    suspend fun printAchievements() {
        val achievements = list()
        achievements.forEach { achievement ->
            println("Achievement: ${achievement.name}, Description: ${achievement.description}")
        }
    }
}