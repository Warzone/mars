package network.warzone.mars.feature

import com.github.kittinunf.result.Result
import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.resource.Resource
import network.warzone.mars.utils.FeatureException

abstract class Service<T : Resource> {

    val apiClient: ApiClient get() = ApiClient

    abstract suspend fun get(target: String): Result<T, FeatureException>

}