package network.warzone.pgm.feature

import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.feature.resource.Resource

interface ResourceSingleton<T : Resource> {
    fun get(): T
}

data class ResourceTarget(val target: String)

abstract class Service<T : Resource> {

    val apiClient: ApiClient = WarzonePGM.instance.apiClient

    abstract suspend fun get(target: String): T

}