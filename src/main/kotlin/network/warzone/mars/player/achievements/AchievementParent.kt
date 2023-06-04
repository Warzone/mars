package network.warzone.mars.player.achievements

/**
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ------------------------------------------------------------------------------
 *     OVERVIEW
 * ------------------------------------------------------------------------------
 * Achievement "parents" are used for categorizing achievements in the GUI.
 *
 * ------------------------------------------------------------------------------
 *     GENERAL ARCHITECTURE
 * ------------------------------------------------------------------------------
 * Every achievement line must have a parent in order to be properly categorized,
 * even if the achievement line only contains a single "milestone" (Ex: Gloves Off).
 *
 * The functionality of an achievement is not affected by its parent; thus,
 * you can ignore this if you don't care about where in the GUI your achievement
 * is placed.
 *
 * ------------------------------------------------------------------------------
 *     IGNORING PARENT DEFINITION
 * ------------------------------------------------------------------------------
 * If ignored, the achievement will simply be assigned "NO_PARENT" and placed in
 * the "MISC" category, together with any other uncategorized achievements.
 *
 * ------------------------------------------------------------------------------
 *     DEFINING A PARENT
 * ------------------------------------------------------------------------------
 * To define a parent for your achievement(s), use a relevant enumerator name
 * followed by the "create" function from the general-use AchievementParentFactory
 * object.
 *
 * Once you input the parameters, you can go into your achievement variant's creation
 * function and specify the parent as the enum you make.
 *
 * ------------------------------------------------------------------------------
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
**/
enum class AchievementParent(val agent: AchievementParentAgent) {
    NO_PARENT(AchievementParentFactory.createAchievementParent(AchievementCategory.MISC, "Other Achievements", "Click to view miscellaneous achievements")),
    PATH_TO_GENOCIDE(AchievementParentFactory.createAchievementParent(AchievementCategory.KILLS, "Path to Genocide")),
    BLOOD_GOD(AchievementParentFactory.createAchievementParent(AchievementCategory.KILLS, "Blood God")),
    BLOOD_BATH(AchievementParentFactory.createAchievementParent(AchievementCategory.KILLS, "Blood Bath")),
    MARKSMAN(AchievementParentFactory.createAchievementParent(AchievementCategory.KILLS, "Marksman"));

    companion object {
        // Maps a parentName to the corresponding AchievementParent enum
        private val map = values().associateBy { it.agent.parentName }
        // Input a string; if it matches the parentName of an AchievementParent's
        // AchievementParentAgent, returns the corresponding AchievementParentAgent.
        fun fromString(name: String): AchievementParentAgent? = map[name]?.agent
    }
}