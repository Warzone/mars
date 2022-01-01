package network.warzone.mars.feature.relations

import network.warzone.mars.feature.Feature
import network.warzone.mars.feature.FeatureManager
import network.warzone.mars.feature.resource.Resource
import network.warzone.mars.feature.resource.ResourceType
import java.util.*

class Relation<T : Resource>(private val type: ResourceType<Feature<T, *>>, val id: UUID) {
    suspend fun get(): T {
        return FeatureManager.getFeature(type).getKnown(id)
    }
}