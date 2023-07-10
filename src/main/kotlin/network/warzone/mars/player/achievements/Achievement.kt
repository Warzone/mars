package network.warzone.mars.player.achievements

import network.warzone.mars.player.achievements.variants.kills.*
import network.warzone.mars.player.achievements.variants.deaths.*
import network.warzone.mars.player.achievements.variants.objectives.*
import network.warzone.mars.player.achievements.variants.wins.*
import network.warzone.mars.player.achievements.variants.losses.*

@Deprecated("Achievements will be created and stored via the API rather than this class")
enum class Achievement(val agentProvider: () -> AchievementAgent) {
    PATH_TO_GENOCIDE_I({ GenocideAchievement.createGenocideAchievement(100, PATH_TO_GENOCIDE_I, "Path to Genocide I") }),
    PATH_TO_GENOCIDE_II({ GenocideAchievement.createGenocideAchievement(250, PATH_TO_GENOCIDE_II, "Path to Genocide II") }),
    PATH_TO_GENOCIDE_III({ GenocideAchievement.createGenocideAchievement(500, PATH_TO_GENOCIDE_III, "Path to Genocide III") }),
    PATH_TO_GENOCIDE_IV({ GenocideAchievement.createGenocideAchievement(750, PATH_TO_GENOCIDE_IV, "Path to Genocide IV") }),
    PATH_TO_GENOCIDE_V({ GenocideAchievement.createGenocideAchievement(1000, PATH_TO_GENOCIDE_V, "Path to Genocide V") }),
    PATH_TO_GENOCIDE_VI({ GenocideAchievement.createGenocideAchievement(2500, PATH_TO_GENOCIDE_VI, "Path to Genocide VI") }),
    PATH_TO_GENOCIDE_VII({ GenocideAchievement.createGenocideAchievement(5000, PATH_TO_GENOCIDE_VII, "Path to Genocide VII") }),
    PATH_TO_GENOCIDE_VIII({ GenocideAchievement.createGenocideAchievement(7500, PATH_TO_GENOCIDE_VIII, "Path to Genocide VIII") }),
    PATH_TO_GENOCIDE_IX({ GenocideAchievement.createGenocideAchievement(10000, PATH_TO_GENOCIDE_IX, "Path to Genocide IX") }),
    PATH_TO_GENOCIDE_X({ GenocideAchievement.createGenocideAchievement(25000, PATH_TO_GENOCIDE_X, "Path to Genocide X") }),

    MARKSMAN_I({ MarksmanAchievement.createMarksmanAchievement(25, MARKSMAN_I, "Marksman I")}),
    MARKSMAN_II({ MarksmanAchievement.createMarksmanAchievement(50, MARKSMAN_II, "Marksman II")}),
    MARKSMAN_III({ MarksmanAchievement.createMarksmanAchievement(75, MARKSMAN_III, "Marksman III")}),
    MARKSMAN_IV({ MarksmanAchievement.createMarksmanAchievement(100, MARKSMAN_IV, "Sniper Steve")}),

    BLOOD_BATH_I({ BloodBathAchievement.createBloodBathAchievement(10, BLOOD_BATH_I, "Blood Bath I")}),
    BLOOD_BATH_II({ BloodBathAchievement.createBloodBathAchievement(25, BLOOD_BATH_II, "Blood Bath II")}),
    BLOOD_BATH_III({ BloodBathAchievement.createBloodBathAchievement(50, BLOOD_BATH_III, "Blood Bath III")}),
    BLOOD_BATH_IV({ BloodBathAchievement.createBloodBathAchievement(75, BLOOD_BATH_IV, "Blood Bath IV")}),
    BLOOD_BATH_V({ BloodBathAchievement.createBloodBathAchievement(100, BLOOD_BATH_V, "Blood Bath V")}),

    WOOL_CAPTURES_I({ WoolCaptureAchievement.createAchievement(100, WOOL_CAPTURES_I, "Woolie Mamoth I")}),
    WOOL_CAPTURES_II({ WoolCaptureAchievement.createAchievement(200, WOOL_CAPTURES_II, "Woolie Mamoth II")}),
    WOOL_CAPTURES_III({ WoolCaptureAchievement.createAchievement(300, WOOL_CAPTURES_III, "Woolie Mamoth III")}),
    WOOL_CAPTURES_IV({ WoolCaptureAchievement.createAchievement(400, WOOL_CAPTURES_IV, "Woolie Mamoth IV")}),
    WOOL_CAPTURES_V({ WoolCaptureAchievement.createAchievement(500, WOOL_CAPTURES_V, "Woolie Mamoth V")}),

    KILL_STREAK_I({ KillstreakAchievement.createAchievement(5, KILL_STREAK_I, "Penta-kill")}),
    KILL_STREAK_II({ KillstreakAchievement.createAchievement(10, KILL_STREAK_II, "Juggernaut")}),
    KILL_STREAK_III({ KillstreakAchievement.createAchievement(25, KILL_STREAK_III, "Untouchable")}),
    KILL_STREAK_IV({ KillstreakAchievement.createAchievement(50, KILL_STREAK_IV, "50-50")}),
    KILL_STREAK_V({ KillstreakAchievement.createAchievement(75, KILL_STREAK_V, "Harbinger")}),
    KILL_STREAK_VI({ KillstreakAchievement.createAchievement(100, KILL_STREAK_VI, "Plot Armor")}),

    BLOOD_GOD({ BloodGodAchievement.createBloodGodAchievement(BLOOD_GOD, "Blood God")}),
    BABY_STEPS({ BabyStepsAchievement.createBabyStepsAchievement(BABY_STEPS, "Baby Steps")}),
    FALLEN_FIGHTER({ FallenFighterAchievement.createAchievement(FALLEN_FIGHTER, "Fallen Fighter")}),
    FIRE_DEATH({ FireDeathAchievement.createAchievement(FIRE_DEATH, "Burnt Toast")}),
    FIRST_DEATH({ FirstDeathAchievement.createAchievement(FIRST_DEATH, "Oof!")}),
    GOLDEN_APPLE_DEATH({ GoldenAppleDeathAchievement.createAchievement(GOLDEN_APPLE_DEATH, "None for All")}),
    SELF_DEATH({ SelfDeathAchievement.createAchievement(SELF_DEATH, "My Own Worst Enemy")}),
}