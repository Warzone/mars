package network.warzone.mars.feature

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

abstract class NamedCachedFeature<T : NamedResource> : CachedFeature<T>() {
    /**
     * Check if cache contains the resource name.
     *
     * **This does not check the API.**
     */
    fun has(name: String) = cache.values.any { it.name.equals(name, ignoreCase = true) }

    /**
     * Get resource from cache.
     */
    fun getCached(name: String) = cache.values.find { it.name.equals(name, ignoreCase = true) }

    /**
     * Returns resource from cache or API with not-null assertion.
     *
     * **This will throw an exception if the resource does not exist.**
     */
    suspend fun getKnown(name: String): T {
        return get(name)!!
    }

    suspend fun get(name: String): T? {
        val local = getCached(name)
        if (local != null) return local

        return fetch(name)
    }
}

abstract class CachedFeature<T : Resource> : Feature<T>() {
    internal val cache: ConcurrentHashMap<UUID, T> = ConcurrentHashMap()

    /**
     * Clear the cache
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Add resource to cache.
     *
     * **This does not create a resource on the API.**
     */
    fun add(resource: T): T {
        cache[resource._id] = resource
        return resource
    }

    /**
     * Remove resource from cache.
     *
     * **This does not remove from API.**
     */
    fun remove(resource: T): Boolean {
        val result = cache.remove(resource._id)
        return result != null
    }

    /**
     * Remove resource ID from cache.
     *
     * **This does not remove from API.**
     */
    fun remove(id: UUID): Boolean {
        val result = cache.remove(id)
        return result != null
    }
    /**
     * Check if cache contains the resource.
     *
     * **This does not check the API.**
     */
    fun has(resource: T): Boolean {
        return cache.containsKey(resource._id)
    }

    /**
     * Check if cache contains the resource ID.
     *
     * **This does not check the API.**
     */
    fun has(id: UUID): Boolean {
        return cache.containsKey(id)
    }

    /**
     * Run a filter predicate on cache.
     */
    fun query(predicate: Predicate<T>): List<T> {
        return cache.values.filter(predicate::test)
    }

    /**
     * Get resource from cache.
     */
    fun getCached(id: UUID): T? {
        return cache[id]
    }

    /**
     * Attempts to get resource from local cache, and API if not in cache.
     *
     * Will add resource to cache if fetched from API.
     * If resolving from cache, data may not be up-to-date.
     */
    override suspend fun get(id: UUID): T? {
        val local = getCached(id)
        if (local != null) return local

        return fetch(id.toString())
    }

    /**
     * Returns resource from cache or API with not-null assertion.
     *
     * **This will throw an exception if the resource does not exist.**
     */
    suspend fun getKnown(id: UUID): T {
        return get(id)!!
    }
}