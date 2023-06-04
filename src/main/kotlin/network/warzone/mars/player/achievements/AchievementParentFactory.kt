package network.warzone.mars.player.achievements

object AchievementParentFactory {
    fun createAchievementParent(categoryType: AchievementCategory, achievementName: String, achievementDescription: String = "Click to view this achievement.") : AchievementParentAgent =
        object : AchievementParentAgent {
            override val category: AchievementCategory = categoryType
            override val parentName: String = achievementName
            override val description: String = achievementDescription
        }
}