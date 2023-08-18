package network.warzone.mars.player.achievements.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AchievementParent {
    abstract val category: String
    abstract val displayName: String
    abstract val description: String

    @Serializable
    @SerialName("NoParent")
    data class NoParent(
        override val category: String = "Misc",
        override val displayName: String = "No Display Name",
        override val description: String = "No Description",
    ) : AchievementParent()

    // Obtain a killstreak of "x"
    @Serializable
    @SerialName("BloodBathParent")
    data class BloodBathParent(
        override val category: String = "Kills",
        override val displayName: String = "Blood Bath",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Capture "x" wools
    @Serializable
    @SerialName("WoolieMammothParent")
    data class WoolieMammothParent(
        override val category: String = "Objectives",
        override val displayName: String = "Woolie Mammoth",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain "x" total kills
    @Serializable
    @SerialName("PathToGenocideParent")
    data class PathToGenocideParent(
        override val category: String = "Kills",
        override val displayName: String = "Path to Genocide",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Shoot and kill a player from "x" blocks away
    @Serializable
    @SerialName("MarksmanParent")
    data class MarksmanParent(
        override val category: String = "Kills",
        override val displayName: String = "Marksman",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Kill "x" players within a span of "y" seconds.
    @Serializable
    @SerialName("MercilessParent")
    data class MercilessParent(
        override val category: String = "Kills",
        override val displayName: String = "Merciless",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Kill "x" players within "y" seconds of each kill.
    @Serializable
    @SerialName("WomboComboParent")
    data class WomboComboParent(
        override val category: String = "Kills",
        override val displayName: String = "Wombo Combo",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Die to a source of fire.
    @Serializable
    @SerialName("BurntToastParent")
    data class BurntToastParent(
        override val category: String = "Deaths",
        override val displayName: String = "Burnt Toast",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain your first first-blood kill.
    @Serializable
    @SerialName("BloodGodParent")
    data class BloodGodParent(
        override val category: String = "Kills",
        override val displayName: String = "Blood God",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain "x" first-blood kills.
    @Serializable
    @SerialName("TotalFirstBloodsParent")
    data class TotalFirstBloodsParent(
        override val category: String = "Kills",
        override val displayName: String = "Swift as the Wind",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Damage "x" monument blocks overall.
    @Serializable
    @SerialName("PillarsOfSandParent")
    data class PillarsOfSandParent(
        override val category: String = "Objectives",
        override val displayName: String = "Pillars of Sand",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Capture "x" flags overall.
    @Serializable
    @SerialName("TouchdownParent")
    data class TouchdownParent(
        override val category: String = "Objectives",
        override val displayName: String = "Touchdown",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Stop "x" flag holders from capturing the flag.
    @Serializable
    @SerialName("PassInterferenceParent")
    data class PassInterferenceParent(
        override val category: String = "Objectives",
        override val displayName: String = "Pass Interference",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Capture "x" control-point objectives overall.
    @Serializable
    @SerialName("TerritorialDisputeParent")
    data class TerritorialDisputeParent(
        override val category: String = "Objectives",
        override val displayName: String = "Territorial Dispute",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Win "x" matches overall.
    @Serializable
    @SerialName("VictoryScreechParent")
    data class VictoryScreechParent(
        override val category: String = "Wins",
        override val displayName: String = "Victory Screech",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Reach level "x".
    @Serializable
    @SerialName("ChampionRoadParent")
    data class ChampionRoadParent(
        override val category: String = "Misc",
        override val displayName: String = "Champion Road",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Play for "x" hours in matches overall.
    @Serializable
    @SerialName("TouchGrassParent")
    data class TouchGrassParent(
        override val category: String = "Misc",
        override val displayName: String = "Touch Grass",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Win your first match.
    @Serializable
    @SerialName("FirstWinParent")
    data class FirstWinParent(
        override val category: String = "Wins",
        override val displayName: String = "Mom, Get the Camera!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain your first kill.
    @Serializable
    @SerialName("FirstKillParent")
    data class FirstKillParent(
        override val category: String = "Kills",
        override val displayName: String = "Baby Steps",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain your first loss.
    @Serializable
    @SerialName("FirstLossParent")
    data class FirstLossParent(
        override val category: String = "Losses",
        override val displayName: String = "My Stats!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // Obtain your first death.
    @Serializable
    @SerialName("FirstDeathParent")
    data class FirstDeathParent(
        override val category: String = "Deaths",
        override val displayName: String = "Oof!",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // ... other subclasses here ...
}
