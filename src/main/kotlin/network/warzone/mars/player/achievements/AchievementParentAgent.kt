package network.warzone.mars.player.achievements

interface AchievementParentAgent{
    val category: AchievementCategory
    val parentName: String
    val description: String
        get() = "Click to view this achievement."
}