package network.warzone.mars.map.images

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import tc.oc.pgm.api.map.MapInfo

class MapImages(
    private val images: List<Pair<String, File>>
) {
    companion object {
        const val CHUNK_SIZE = 32

        fun getMapImage(map: MapInfo) : Pair<String, File>? {
            val file = File(map.source.absoluteDir.toFile(), "map.png")
            return if (file.exists()) map.id to file else null
        }
    }

    suspend fun streamToChannel(
        scope: CoroutineScope,
        channel: Channel<ByteArray>,
        chunkSize: Int = CHUNK_SIZE
    ) {
        images.chunked(chunkSize).forEach { chunk ->
            chunk.map { (mapName, file) ->
                scope.launch {
                    val nameBytes = mapName.toByteArray()

                    val bos = ByteArrayOutputStream()
                    val dos = DataOutputStream(bos)
                    val imageContent = file.readBytes()

                    dos.writeInt(Integer.BYTES + nameBytes.size + imageContent.size)
                    dos.writeInt(nameBytes.size)
                    dos.write(nameBytes)
                    dos.write(imageContent)

                    channel.send(bos.toByteArray())
                }
            }.joinAll()
        }
        channel.close()
    }
}