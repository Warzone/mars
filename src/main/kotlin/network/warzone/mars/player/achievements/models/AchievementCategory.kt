package network.warzone.mars.player.achievements.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// A class for defining achievement categories, which are used solely to categorize
// achievements within the GUI.
@Serializable
data class AchievementCategory(val category: String, val displayName: String, val description: String)