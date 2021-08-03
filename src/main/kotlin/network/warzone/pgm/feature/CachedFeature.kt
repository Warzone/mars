package network.warzone.pgm.feature

import network.warzone.pgm.feature.resource.Resource
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

abstract class CachedFeature<T : Resource, U : Service<T>> : Feature<T, U>() {

    internal val cache: ConcurrentHashMap<UUID, T> = ConcurrentHashMap()

    fun sync(resources: List<T>): ConcurrentHashMap<UUID, T> {
        cache.clear()

        resources
           .associateBy { value -> value._id }
           .let { cache.putAll(it) }

        return cache
    }

    fun add(resource: T): T {
        resource.generate()

        cache[resource._id] = resource

        return resource
    }

    fun has(id: UUID): Boolean {
        return cache.containsKey(id)
    }

    fun query(predicate: Predicate<T>): List<T> {
        return cache.values.filter(predicate::test)
    }

    fun invalidate(id: UUID) {
        cache.remove(id)
    }

    override suspend fun get(uuid: UUID): T {
        if (has(uuid)) return cache.getValue(uuid)

        return service
            .get(target = uuid.toString())
            .also { add(it) }
    }

}