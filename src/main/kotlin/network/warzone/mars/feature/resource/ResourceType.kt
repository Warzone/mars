package network.warzone.mars.feature.resource

import network.warzone.mars.feature.Feature
import network.warzone.mars.map.MapFeature
import network.warzone.mars.player.feature.PlayerFeature
import network.warzone.mars.ranks.RankFeature
import network.warzone.mars.tags.TagFeature
import kotlin.reflect.KClass

sealed class ResourceType<out T : Feature<*, *>>(type: KClass<T>) {

    object Player : ResourceType<PlayerFeature>(PlayerFeature::class)
    object Rank : ResourceType<RankFeature>(RankFeature::class)
    object Tag : ResourceType<TagFeature>(TagFeature::class)
    object Map : ResourceType<MapFeature>(MapFeature::class)

    private val type: Class<T>

    init {
        this.type = type.java
    }

    fun cast(obj: Any): T {
        return type.cast(obj)
    }

}