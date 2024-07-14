package network.warzone.mars.player.models

import network.warzone.api.database.models.AchievementStatistic
import network.warzone.mars.api.socket.models.SimplePlayer
import network.warzone.mars.feature.NamedResource
import network.warzone.mars.punishment.models.StaffNote
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.TagFeature
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.color
import org.bukkit.ChatColor
import tc.oc.pgm.api.map.Gamemode
import java.text.NumberFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.pow


data class PlayerProfile(
    override val _id: UUID,

    override val name: String,
    val nameLower: String,

    val firstJoinedAt: Date,
    val lastJoinedAt: Date,

    val ips: List<String>,
    var notes: List<StaffNote>,

    val stats: PlayerStats,
    val gamemodeStats: HashMap<Gamemode, PlayerStats>,

    val rankIds: MutableList<UUID>,

    val tagIds: MutableList<UUID>,

    var activeTagId: UUID?,
    var activeJoinSoundId: String? = null
) : NamedResource {
    suspend fun tags(): List<Tag> = tagIds.mapNotNull { TagFeature.get(it) }
    suspend fun ranks(): List<Rank> = rankIds.mapNotNull { RankFeature.get(it) }
    suspend fun activeTag(): Tag? {
        if (activeTagId == null) return null
        return TagFeature.get(activeTagId!!)
    }

    private suspend fun getRankDisplay(): String? {
        val rank = ranks().filter { it.prefix != null }.maxByOrNull { it.priority }
        return rank?.prefix?.color()
    }

    private suspend fun getTagDisplay(): String? {
        val tagDisplay = activeTag()?.display?.color() ?: return null
        return "${ChatColor.GRAY}[${tagDisplay}${ChatColor.GRAY}]"
    }

    suspend fun getDisplayName(nameColor: ChatColor) =
        "${getRankDisplay() ?: ""} ${nameColor}$name ${getTagDisplay() ?: ""}".trim()

}

data class PlayerStats(
    var xp: Int = 0,
    var serverPlaytime: Long = 0,
    var gamePlaytime: Long = 0,
    var kills: Int = 0,
    var deaths: Int = 0,
    var voidKills: Int = 0,
    var voidDeaths: Int = 0,
    var firstBloods: Int = 0,
    var firstBloodsSuffered: Int = 0,
    val objectives: PlayerObjectiveStatistics = PlayerObjectiveStatistics(),
    var bowShotsTaken: Int = 0,
    var bowShotsHit: Int = 0,
    val blocksPlaced: HashMap<String, Int> = hashMapOf(),
    val blocksBroken: HashMap<String, Int> = hashMapOf(),
    var damageTaken: Double = 0.0,
    var damageGiven: Double = 0.0,
    var damageGivenBow: Double = 0.0,
    val messages: PlayerMessages = PlayerMessages(),
    var wins: Int = 0,
    var losses: Int = 0,
    var ties: Int = 0,
    var matches: Int = 0,
    var matchesPresentStart: Int = 0,
    var matchesPresentFull: Int = 0,
    var matchesPresentEnd: Int = 0,
    var mvps: Int = 0, // todo
    val records: PlayerRecords = PlayerRecords(), // todo
    val weaponKills: MutableMap<String, Int> = mutableMapOf(),
    val weaponDeaths: MutableMap<String, Int> = mutableMapOf(),
    val killstreaks: MutableMap<Int, Int> = mutableMapOf(5 to 0, 10 to 0, 25 to 0, 50 to 0, 100 to 0),
    val achievements: MutableMap<String, AchievementStatistic> = mutableMapOf()
) {

    companion object {
        interface ExpFormula {
            fun getLevelFromExp(exp: Double) : Int
            fun getExpRequiredForLevel(level: Int) : Double
            fun getExactLevel(exp: Double) : Float
            fun getLevelProgress(exp: Double): Float
        }

        class PowerExpFormula(
            private val linearFactor: Double,
            private val growthFactor: Double
        ) : ExpFormula {
            override fun getLevelFromExp(exp: Double): Int =
                floor(linearFactor * exp.pow(growthFactor)).toInt() + 1

            override fun getExpRequiredForLevel(level: Int) : Double {
                if (level <= 1) return 0.0
                return (((level - 1).toDouble())/(linearFactor)).pow(1.0/growthFactor)
            }

            override fun getExactLevel(exp: Double): Float =
                (linearFactor * exp.pow(growthFactor)).toFloat() % 1f

            override fun getLevelProgress(exp: Double): Float =
                getExactLevel(exp) % 1f
        }

        class LinearExpFormula(
            private val step: Int
        ) : ExpFormula {
            override fun getLevelFromExp(exp: Double): Int =
                floor(((exp + step) / step).toDouble()).toInt()

            override fun getExpRequiredForLevel(level: Int) : Double {
                if (level <= 1) return 0.0
                return ((level * step) - step).toDouble()
            }

            override fun getExactLevel(exp: Double): Float =
                (exp / step).toFloat()

            override fun getLevelProgress(exp: Double): Float =
                ((exp % step) / step).toFloat()
        }

        private val LINEAR_FORMULA = LinearExpFormula(5000)
        private val EXPONENTIAL_FORMULA = PowerExpFormula(0.0056, 0.715)
        var EXP_FORMULA : ExpFormula = LINEAR_FORMULA
            private set

        fun useExponentialExp(use: Boolean) {
            EXP_FORMULA = if (use) EXPONENTIAL_FORMULA else LINEAR_FORMULA
        }
    }
    val level: Int
        get() = EXP_FORMULA.getLevelFromExp(xp.toDouble())

    val winPercentage: String
        get() {
            val nf = NumberFormat.getInstance()
            nf.maximumFractionDigits = 1
            nf.minimumFractionDigits = 1
            return if (matches == 0) "0%" else "${nf.format(wins.toDouble() / matchesPresentEnd * 100f)}%"
        }

    val kdr: String
        get() {
            val nf = NumberFormat.getInstance()
            nf.maximumFractionDigits = 2
            nf.minimumFractionDigits = 2
            return if (deaths == 0) nf.format(kills.toDouble()) else nf.format(kills.toDouble() / deaths)
        }
}

data class PlayerRecords(
//    var highestKillstreak: Int = 0, -- this can be calculated from profile
    var longestSession: SessionRecord? = null,
//    var longestProjectileHit: ProjectileRecord? = null,
    var longestProjectileKill: ProjectileRecord? = null,
    var fastestWoolCapture: LongRecord? = null,
    var fastestFlagCapture: LongRecord? = null,
    var fastestFirstBlood: FirstBloodRecord? = null,
    var killsInMatch: IntRecord? = null,
    var deathsInMatch: IntRecord? = null,
)

data class SessionRecord(val sessionId: String, val length: Long)

data class ProjectileRecord(val matchId: String, val distance: Int)

data class FirstBloodRecord(val matchId: String, val victim: SimplePlayer, val time: Long)

data class IntRecord(val matchId: String, val value: Int)

data class LongRecord(val matchId: String, val value: Long)

data class PlayerObjectiveStatistics(
    var coreLeaks: Int = 0,
    var destroyableDestroys: Int = 0,
    var destroyableBlockDestroys: Int = 0,
    var flagCaptures: Int = 0,
    var flagPickups: Int = 0,
    var flagDrops: Int = 0,
    var flagDefends: Int = 0,
    var totalFlagHoldTime: Long = 0,
    var woolCaptures: Int = 0,
    var woolDrops: Int = 0,
    var woolDefends: Int = 0,
    var woolPickups: Int = 0,
    var controlPointCaptures: Int = 0
)

data class PlayerMessages(var staff: Int = 0, var global: Int = 0, var team: Int = 0)
