package network.warzone.mars.feature.named

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrNull
import com.github.kittinunf.result.map
import network.warzone.mars.feature.CachedFeature
import network.warzone.mars.feature.Service
import network.warzone.mars.utils.FeatureException

abstract class NamedCacheFeature<T : NamedResource, U : Service<T>> : CachedFeature<T, U>() {
    fun has(name: String): Boolean {
        return cache.values.any { it.name.equals(name, ignoreCase = true) }
    }

    fun getCached(name: String): T? = cache.values.firstOrNull { it.name.equals(name, ignoreCase = true) }

    suspend fun getKnown(name: String): T = getOrNull(name)!!
    suspend fun getOrNull(name: String): T? = get(name).getOrNull()
    suspend fun getOrThrow(name: String): T? = get(name).get()

    suspend fun get(name: String): Result<T, FeatureException> {
        cache.values
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.let { return Result.success(it) }

        return service.get(name)
            .map {
                it.generate()
                add(it)
            }
    }
}