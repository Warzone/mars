package network.warzone.mars.player.achievements.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO: Change this to be defined via the API rather than hard-coded.

// A class for defining achievement categories, which are used solely to categorize
// achievements within the GUI.
@Serializable
sealed class AchievementCategory {
    abstract val category: String
    abstract val displayName: String
    abstract val description: String

    // Note: Explicit SerialName is used because default class path
    // may not match API package.
    @Serializable
    @SerialName("NoCategory")
    data class NoCategory(
        override val category: String = "Misc",
        override val displayName: String = "No Display Name",
        override val description: String = "No Description",
    ) : AchievementCategory()

    // Obtain a killstreak of "x"
    @Serializable
    @SerialName("BloodBathCategory")
    data class BloodBathCategory(
        override val category: String = "Kills",
        override val displayName: String = "Blood Bath",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Capture "x" wools
    @Serializable
    @SerialName("WoolieMammothCategory")
    data class WoolieMammothCategory(
        override val category: String = "Objectives",
        override val displayName: String = "Woolie Mammoth",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain "x" total kills
    @Serializable
    @SerialName("PathToGenocideCategory")
    data class PathToGenocideCategory(
        override val category: String = "Kills",
        override val displayName: String = "Path to Genocide",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Shoot and kill a player from "x" blocks away
    @Serializable
    @SerialName("MarksmanCategory")
    data class MarksmanCategory(
        override val category: String = "Kills",
        override val displayName: String = "Marksman",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Kill "x" players within a span of "y" seconds.
    @Serializable
    @SerialName("MercilessCategory")
    data class MercilessCategory(
        override val category: String = "Kills",
        override val displayName: String = "Merciless",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Kill "x" players within "y" seconds of each kill.
    @Serializable
    @SerialName("WomboComboCategory")
    data class WomboComboCategory(
        override val category: String = "Kills",
        override val displayName: String = "Wombo Combo",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Die to a source of fire.
    @Serializable
    @SerialName("BurntToastCategory")
    data class BurntToastCategory(
        override val category: String = "Deaths",
        override val displayName: String = "Burnt Toast",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain your first first-blood kill.
    @Serializable
    @SerialName("BloodGodCategory")
    data class BloodGodCategory(
        override val category: String = "Kills",
        override val displayName: String = "Blood God",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain "x" first-blood kills.
    @Serializable
    @SerialName("TotalFirstBloodsCategory")
    data class TotalFirstBloodsCategory(
        override val category: String = "Kills",
        override val displayName: String = "Swift as the Wind",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Damage "x" monument blocks overall.
    @Serializable
    @SerialName("PillarsOfSandCategory")
    data class PillarsOfSandCategory(
        override val category: String = "Objectives",
        override val displayName: String = "Pillars of Sand",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Capture "x" flags overall.
    @Serializable
    @SerialName("TouchdownCategory")
    data class TouchdownCategory(
        override val category: String = "Objectives",
        override val displayName: String = "Touchdown",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Stop "x" flag holders from capturing the flag.
    @Serializable
    @SerialName("PassInterferenceCategory")
    data class PassInterferenceCategory(
        override val category: String = "Objectives",
        override val displayName: String = "Pass Interference",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Capture "x" control-point objectives overall.
    @Serializable
    @SerialName("TerritorialDisputeCategory")
    data class TerritorialDisputeCategory(
        override val category: String = "Objectives",
        override val displayName: String = "Territorial Dispute",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Win "x" matches overall.
    @Serializable
    @SerialName("VictoryScreechCategory")
    data class VictoryScreechCategory(
        override val category: String = "Wins",
        override val displayName: String = "Victory Screech",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Reach level "x".
    @Serializable
    @SerialName("ChampionRoadCategory")
    data class ChampionRoadCategory(
        override val category: String = "Misc",
        override val displayName: String = "Champion Road",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Play for "x" hours in matches overall.
    @Serializable
    @SerialName("TouchGrassCategory")
    data class TouchGrassCategory(
        override val category: String = "Misc",
        override val displayName: String = "Touch Grass",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Win your first match.
    @Serializable
    @SerialName("FirstWinCategory")
    data class FirstWinCategory(
        override val category: String = "Wins",
        override val displayName: String = "Mom, Get the Camera!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain your first kill.
    @Serializable
    @SerialName("FirstKillCategory")
    data class FirstKillCategory(
        override val category: String = "Kills",
        override val displayName: String = "Baby Steps",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain your first loss.
    @Serializable
    @SerialName("FirstLossCategory")
    data class FirstLossCategory(
        override val category: String = "Losses",
        override val displayName: String = "My Stats!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // Obtain your first death.
    @Serializable
    @SerialName("FirstDeathCategory")
    data class FirstDeathCategory(
        override val category: String = "Deaths",
        override val displayName: String = "Oof!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementCategory()

    // ... other subclasses here ...
}