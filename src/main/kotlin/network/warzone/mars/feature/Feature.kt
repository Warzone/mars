package network.warzone.mars.feature

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getOrNull
import network.warzone.mars.feature.resource.Resource
import network.warzone.mars.utils.FeatureException
import java.util.*

abstract class Feature<T : Resource, U : Service<T>> {

    abstract val service: U

    abstract suspend fun get(uuid: UUID): Result<T, FeatureException>
    abstract fun getCached(uuid: UUID): T?

    suspend fun getKnown(uuid: UUID): T = getOrNull(uuid)!!
    suspend fun getOrNull(uuid: UUID): T? = get(uuid).getOrNull()
    suspend fun getOrThrow(uuid: UUID): T? = get(uuid).get()

    /**
     * Invoked when the websocket connects, signals the feature is allowed to fetch any starting resources it needs.
     */
    open suspend fun init() {}

    open fun getSubcommands(): Map<List<String>, Any> {
        return emptyMap()
    }

    open fun getCommands(): List<Any> {
        return ArrayList()
    }

}