package network.warzone.mars.feature

import network.warzone.mars.map.MapFeature
import java.util.*
import kotlin.reflect.KClass

sealed class ResourceType<out T : Feature<*>>(type: KClass<T>) {
    object Tag : ResourceType<MapFeature>(MapFeature::class)
    object Map : ResourceType<MapFeature>(MapFeature::class)
    object Player : ResourceType<MapFeature>(MapFeature::class)
    object Rank : ResourceType<MapFeature>(MapFeature::class)
    object Punishment : ResourceType<MapFeature>(MapFeature::class)

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