package network.warzone.mars.player.achievements

enum class AchievementCategory(val displayName: String){
    KILLS("Kills"),
    DEATHS("Deaths"),
    WINS("Wins"),
    LOSSES("Losses"),
    OBJECTIVES("Objectives"),
    MISC("Misc");

    companion object {
        // Simply maps the AchievementCategory enum to its displayName
        val map = values().associateBy(AchievementCategory::displayName)
        // Search for a target displayName string and return a corresponding
        // AchievementCategory enum if found.
        fun fromString(displayName: String): AchievementCategory? = map[displayName]
    }
}