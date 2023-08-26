package network.warzone.mars.feature

import network.warzone.mars.broadcast.BroadcastFeature
import network.warzone.mars.map.MapFeature
import network.warzone.mars.player.achievements.AchievementFeature
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.punishment.PunishmentFeature
import network.warzone.mars.rank.RankFeature
import network.warzone.mars.report.ReportFeature
import network.warzone.mars.tag.TagFeature
import java.util.*
import kotlin.reflect.KClass

sealed class ResourceType<out T : Feature<*>>(type: KClass<T>) {
    object Achievement : ResourceType<AchievementFeature>(AchievementFeature::class)
    object Tag : ResourceType<TagFeature>(TagFeature::class)
    object Map : ResourceType<MapFeature>(MapFeature::class)
    object Player : ResourceType<PlayerFeature>(PlayerFeature::class)
    object Rank : ResourceType<RankFeature>(RankFeature::class)
    object Punishment : ResourceType<PunishmentFeature>(PunishmentFeature::class)
    object Broadcast : ResourceType<BroadcastFeature>(BroadcastFeature::class)
    object Report : ResourceType<ReportFeature>(ReportFeature::class)

    private val type: Class<T>

    init {
        this.type = type.java
    }

    fun cast(obj: Any): T {
        return type.cast(obj)
    }
}

interface Resource {
    val _id: UUID
}

interface NamedResource : Resource {
    val name: String
}