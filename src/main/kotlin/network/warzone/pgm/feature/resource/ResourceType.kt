package network.warzone.pgm.feature.resource

import network.warzone.pgm.feature.Feature
import network.warzone.pgm.player.feature.PlayerFeature
import network.warzone.pgm.ranks.RankFeature
import kotlin.reflect.KClass

sealed class ResourceType<out T : Feature<*, *>>(type: KClass<T>) {

    object Player : ResourceType<PlayerFeature>(PlayerFeature::class)
    object Rank : ResourceType<RankFeature>(RankFeature::class)

    private val type: Class<T>

    init {
        this.type = type.java
    }

    fun cast(obj: Any): T {
        return type.cast(obj)
    }

}