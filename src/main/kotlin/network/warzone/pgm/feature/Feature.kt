package network.warzone.pgm.feature

import network.warzone.pgm.feature.resource.Resource
import java.util.*

abstract class Feature<T : Resource, U : Service<T>> {

    abstract val service: U

    abstract suspend fun get(uuid: UUID): T

    /**
     * Invoked when the websocket connects, signals the feature is allowed to fetch any starting resources it needs.
     */
    open suspend fun init() {}

    open fun getCommands(): List<Any> {
        return ArrayList()
    }

}