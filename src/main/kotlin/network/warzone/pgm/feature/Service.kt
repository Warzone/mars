package network.warzone.pgm.feature

import com.github.kittinunf.result.Result
import network.warzone.pgm.WarzonePGM
import network.warzone.pgm.api.ApiClient
import network.warzone.pgm.feature.resource.Resource
import network.warzone.pgm.utils.FeatureException

abstract class Service<T : Resource> {

    val apiClient: ApiClient get() = WarzonePGM.get().apiClient

    abstract suspend fun get(target: String): Result<T, FeatureException>

}