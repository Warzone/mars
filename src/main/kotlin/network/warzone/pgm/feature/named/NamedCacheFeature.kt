package network.warzone.pgm.feature.named

import network.warzone.pgm.feature.CachedFeature
import network.warzone.pgm.feature.Service
import network.warzone.pgm.feature.resource.Resource

abstract class NamedCacheFeature<T : NamedResource, U : Service<T>> : CachedFeature<T, U>() {

    suspend fun get(name: String): T {
        cache.values
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.let { return it }

        return service.get(name)
            .also(Resource::generate)
            .also(::add)
    }

}