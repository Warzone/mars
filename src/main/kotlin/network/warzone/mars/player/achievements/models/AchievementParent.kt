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

    @Serializable
    @SerialName("BloodBathParent")
    data class BloodBathParent(
        override val category: String = "Kills",
        override val displayName: String = "Blood Bath",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    @Serializable
    @SerialName("WoolieMammothParent")
    data class WoolieMammothParent(
        override val category: String = "Objectives",
        override val displayName: String = "Woolie Mammoth",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    @Serializable
    @SerialName("PathToGenocideParent")
    data class PathToGenocideParent(
        override val category: String = "Kills",
        override val displayName: String = "Path to Genocide",
        override val description: String = "Click here to view this achievement."
    ) : AchievementParent()

    // ... other subclasses here ...
}