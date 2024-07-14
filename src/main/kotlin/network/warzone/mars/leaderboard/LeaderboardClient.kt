package network.warzone.mars.leaderboard

import network.warzone.mars.api.ApiClient

object LeaderboardClient {
    suspend fun fetchLeaderboardEntries(scoreType: LeaderboardScoreType, period: LeaderboardPeriod) : List<LeaderboardEntry> {
        return ApiClient.get("/mc/leaderboards/${scoreType.name}/${period.name}")
    }
}

data class LeaderboardEntry(
    val id: String,
    val name: String,
    val score: Int
)

enum class LeaderboardScoreType {
    KILLS, DEATHS, XP, WINS, LOSSES
}

enum class LeaderboardPeriod {
    ALL_TIME, DAILY, WEEKLY, MONTHLY, YEARLY
}