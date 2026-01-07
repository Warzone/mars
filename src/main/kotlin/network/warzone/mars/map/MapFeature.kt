package network.warzone.mars.map

import io.ktor.utils.io.close
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import java.io.File
import java.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import network.warzone.mars.Mars
import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.NamedCachedFeature
import network.warzone.mars.map.images.MapImages
import network.warzone.mars.map.images.MapImages.Companion.CHUNK_SIZE
import network.warzone.mars.map.images.MapImages.Companion.getMapImage
import network.warzone.mars.map.models.GameMap
import network.warzone.mars.map.models.MapContributor
import network.warzone.mars.utils.BASE_LOGGER
import org.bukkit.Bukkit
import tc.oc.pgm.api.PGM
import tc.oc.pgm.api.map.Contributor
import tc.oc.pgm.api.map.MapInfo
import tc.oc.pgm.map.contrib.PlayerContributor

object MapFeature : NamedCachedFeature<GameMap>() {
    override suspend fun init() {
        findNewMaps()
    }

    override suspend fun fetch(target: String): GameMap? {
        return try {
            ApiClient.get("/mc/maps/$target")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun create(maps: List<MapLoadOneRequest>): List<GameMap> {
        val created = ApiClient.post<List<GameMap>, List<MapLoadOneRequest>>("/mc/maps", maps)

        return created.onEach { add(it) }
    }

    private fun uploadImages(imageFiles: List<Pair<String, File>>) {
        Mars.get().coroutines.launch {
            val readEnd = writer {
                val conduit = Channel<ByteArray>(capacity = CHUNK_SIZE)
                val images = MapImages(imageFiles)
                launch {
                    images.streamToChannel(this, conduit)
                }
                launch {
                    for (data in conduit) {
                        channel.writeFully(data)
                    }
                    channel.flush()
                    channel.close()
                }
            }.channel
            ApiClient.postBinary<Unit>(
                "/mc/maps/images",
                readEnd
            )
        }
    }

    suspend fun list(): List<GameMap> {
        val maps = ApiClient.get<List<GameMap>>("/mc/maps")

        return maps.onEach { add(it) }
    }

    /**
     * Find and load all new maps.
     */
    private suspend fun findNewMaps() {
        // Get the PGM Map Library.
        val pgmMaps = PGM.get().mapLibrary.maps

        // Initialize an empty array of map load requests.
        val mapLoadRequests = mutableListOf<MapLoadOneRequest>()
        val mapImages = mutableListOf<Pair<String, File>>()

        // Loop over the iterator of maps.
        while (pgmMaps.hasNext()) {
            // Get the map.
            val map = pgmMaps.next()

            if (map.gamemodes.isEmpty()) Bukkit.getLogger().warning("Found map '${map.name}' with no registered gamemodes")

            mapLoadRequests.add(toMapLoadRequest(map, null))
            getMapImage(map)?.let { mapImages.add(it) }
        }

        // Send all the map load requests to the API.
        create(maps = mapLoadRequests)
        BASE_LOGGER.info("Found ${mapImages.size} map image(s) to upload")
        uploadImages(mapImages)
    }

    private fun toMapLoadRequest(map: MapInfo, id: UUID?): MapLoadOneRequest {
        return MapLoadOneRequest(
            _id = id ?: UUID.randomUUID(),
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

    private fun GameMap.hasChanged(map: MapInfo): Boolean {
        return !this.name.equals(map.name, ignoreCase = true) ||
                this.version != map.version.toString() ||
                this.gamemodes != map.gamemodes.map { it.name } ||
                this.authors != map.authors.mapNotNull { resolveContributor(it) } ||
                this.contributors != map.contributors.mapNotNull { resolveContributor(it) }
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