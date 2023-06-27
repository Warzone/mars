package network.warzone.mars.player.achievements

import network.warzone.api.database.models.Achievement
import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.Feature

object AchievementFeature : Feature<Achievement>() {
    // TODO: The /mc/achievements GET route returns a 'List<Achievement>'.
    //  I imagine this is going to cause problems since fetch is trying to
    //  return 'Achievement?', but I couldn't set the return type to the list.
    //
    //  Unless I'm misunderstanding something?
    override suspend fun fetch(target: String): Achievement? {
        return try {
            ApiClient.get("/mc/achievements")
        } catch (e: Exception) {
            null
        }
    }
}