package network.warzone.pgm.feature.relations

import kotlinx.coroutines.runBlocking
import network.warzone.pgm.feature.Feature
import network.warzone.pgm.feature.FeatureManager
import network.warzone.pgm.feature.resource.Resource
import network.warzone.pgm.feature.resource.ResourceType
import java.util.*

class Relation<T : Resource>(private val type: ResourceType<Feature<T, *>>, private val id: UUID) {

    fun blockAndGet(): T = runBlocking {
        return@runBlocking FeatureManager.getFeature(type).get(id)
    }

    suspend fun get(): T {
        return FeatureManager.getFeature(type).get(id)
    }

}