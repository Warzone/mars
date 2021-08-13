package network.warzone.pgm.map

import network.warzone.pgm.feature.named.NamedCacheFeature
import network.warzone.pgm.map.models.GameMap
import network.warzone.pgm.map.models.MapContributor
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.map.Contributor
import tc.oc.pgm.api.map.MapInfo
import tc.oc.pgm.map.contrib.PlayerContributor
import tc.oc.pgm.util.Version
import java.util.*

object MapFeature : NamedCacheFeature<GameMap, MapService>() {
    override val service = MapService

    override suspend fun init() {
        findNewMaps()
    }

    suspend fun create(maps: List<MapService.MapLoadOneRequest>): List<GameMap> {
        return service
            .create(maps)
            .also(::sync)
    }

    suspend fun list(): List<GameMap> {
        return service
            .list()
            .also(::sync)
    }

    /**
     * Find and load all new maps.
     */
    suspend fun findNewMaps() {
        // Get the currently loaded maps.
        val currentMaps: List<GameMap> = list()

        // Get the PGM Map Library.
        val pgmMaps = PGM.get().mapLibrary.maps

        // Initialize an empty array of map load requests.
        val mapLoadRequests = mutableListOf<MapService.MapLoadOneRequest>()

        // Loop over the iterator of maps.
        while (pgmMaps.hasNext()) {
            // Get the map.
            val map = pgmMaps.next()

            // Try and find a currently loaded map by the same name.
            val existingMap = currentMaps.find { it.name == map.name }

            // Indicate we should load the map if it isn't currently loaded.
            var load = existingMap == null

            // If it is currently loaded, check its version.
            if (existingMap != null) {
                // Get the current maps version components.
                val components = existingMap.version.split(".").map { it.toInt() }
                val existingVersion: Version = if (components.size == 3) {
                    val (major, minor, patch) = components
                    Version(major, minor, patch)
                } else {
                    val (major, minor) = components
                    Version(major, minor, 0)
                }

                // Indicate we should load the map if the existing version is older than the new version.
                load = existingVersion.isOlderThan(map.version)
            }

            // If indicated we should load, convert the map into a map load request and add it the list.
            if (load) mapLoadRequests.add(toMapLoadRequest(map))
        }

        // Send all the map load requests to the API.
        create(maps = mapLoadRequests)
    }

    private fun toMapLoadRequest(map: MapInfo): MapService.MapLoadOneRequest {
        return MapService.MapLoadOneRequest(
            _id = UUID.randomUUID(),
            name = map.name,
            version = map.version.toString(),
            gamemodes = map.gamemodes.map { it.name },
            authors = map.authors.mapNotNull { resolveContributor(it) },
            contributors = map.contributors.mapNotNull { resolveContributor(it) }
        )
    }

    private fun resolveContributor(contributor: Contributor): MapContributor? {
        return when (contributor) {
            is PlayerContributor -> MapContributor(contributor.id, contributor.contribution)
            else -> null
        }
    }
}