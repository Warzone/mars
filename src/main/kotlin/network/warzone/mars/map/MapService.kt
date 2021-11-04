package network.warzone.mars.map

import com.github.kittinunf.result.Result
import network.warzone.mars.api.http.ApiExceptionType
import network.warzone.mars.feature.Service
import network.warzone.mars.map.exceptions.MapMissingException
import network.warzone.mars.map.models.GameMap
import network.warzone.mars.map.models.MapContributor
import network.warzone.mars.utils.FeatureException
import network.warzone.mars.utils.mapErrorSmart
import network.warzone.mars.utils.parseHttpException
import java.util.*

object MapService : Service<GameMap>() {

    suspend fun create(
        maps: List<MapLoadOneRequest>
    ): List<GameMap> {
        return apiClient.post("/mc/maps", maps)
    }

    suspend fun list(): List<GameMap> {
        return apiClient.get("/mc/maps")
    }

    override suspend fun get(target: String): Result<GameMap, FeatureException> {
        return parseHttpException {
            apiClient.get<GameMap>("/mc/maps/$target")
        }.mapErrorSmart {
            when (it.code) {
                ApiExceptionType.MAP_MISSING -> MapMissingException(target)
                else -> TODO()
            }
        }
    }

    data class MapLoadOneRequest(
        val _id: UUID,
        val name: String,
        val version: String,
        val gamemodes: List<String>,
        val authors: List<MapContributor>,
        val contributors: List<MapContributor>
    )

}