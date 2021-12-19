package network.warzone.mars.player.models

import kotlinx.serialization.Serializable
import network.warzone.mars.feature.named.NamedResource
import network.warzone.mars.feature.relations.Relation
import network.warzone.mars.feature.resource.ResourceType
import network.warzone.mars.punishment.models.StaffNote
import network.warzone.mars.rank.models.Rank
import network.warzone.mars.tag.models.Tag
import network.warzone.mars.utils.color
import org.bukkit.ChatColor
import java.util.*

data class PlayerProfile(
    override val _id: UUID,

    override val name: String,
    val nameLower: String,

    val firstJoinedAt: Date,
    val lastJoinedAt: Date,

    val ips: List<String>,
    var notes: List<StaffNote>,

    val stats: PlayerStats,

    val rankIds: MutableList<UUID>,
    @Transient var ranks: List<Relation<Rank>> = emptyList(),

    var activeTagId: UUID?,
    @Transient var activeTag: Relation<Tag>? = null,

    val tagIds: MutableList<UUID>,
    @Transient var tags: List<Relation<Tag>> = emptyList()
) : NamedResource {

    suspend fun tags(): List<Tag> = tags.map { it.get() }
    suspend fun ranks(): List<Rank> = ranks.map { it.get() }
    suspend fun activeTag(): Tag? = activeTag?.get()

    override fun generate(): PlayerProfile {
        ranks = rankIds.map {
            Relation(ResourceType.Rank, it)
        }

        tags = tagIds.map {
            Relation(ResourceType.Tag, it)
        }

        activeTag = null
        activeTagId?.let {
            activeTag = Relation(ResourceType.Tag, it)
        }

        return this
    }

    suspend fun getRankPrefix(): String? {
        val rank: Rank? = ranks()
            .filter { it.prefix != null }
            .maxByOrNull { it.priority }

        return rank?.prefix?.color()
    }

    suspend fun getSuffix(): String? {
        val tagDisplay = activeTag()?.display?.color() ?: return null
        return "${ChatColor.GRAY}[${tagDisplay}${ChatColor.GRAY}]"
    }

    suspend fun getDisplayName(nameColour: ChatColor): String {
        return "${getRankPrefix() ?: ""} ${nameColour}${name} ${getSuffix() ?: ""}".trim()
    }
}

data class PlayerStats(
    var xp: Int = 0, // todo
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
    val killstreaks: MutableMap<Int, Int> = mutableMapOf(5 to 0, 10 to 0, 25 to 0, 50 to 0, 100 to 0),
)

data class PlayerRecords(
    var highestKillstreak: Int = 0,
    var longestSession: Int = 0,
    var longestBowShot: Int = 0,
    var fastestWoolCapture: Int = 0,
    var fastestFirstBlood: Int = 0,
    var fastestFlagCapture: Int = 0,
    var killsPerMatch: Int = 0,
    var deathsInMatch: Int = 0,
    var highestScore: Int = 0
)

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