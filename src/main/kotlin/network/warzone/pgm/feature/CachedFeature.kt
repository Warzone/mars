package network.warzone.pgm.feature

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import network.warzone.pgm.feature.resource.Resource
import network.warzone.pgm.utils.FeatureException
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

    /**
     * Sets a [UUID] to a resource [T].
     *
     * @param id The [UUID] to set.
     * @param resource The [T] resource to set it to.
     *
     * @return The resource [T]
     */
    fun set(id: UUID, resource: T): T {
        cache[id] = resource

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

    override fun getCached(uuid: UUID): T? {
        return cache[uuid]
    }

    override suspend fun get(uuid: UUID): Result<T, FeatureException> {
        if (has(uuid)) return Result.success(cache.getValue(uuid))

        return service
            .get(target = uuid.toString())
            .map { add(it) }
    }

}