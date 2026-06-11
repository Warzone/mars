package network.warzone.mars.map.images

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import tc.oc.pgm.api.map.MapInfo

class MapImages(
    private val images: List<Pair<String, File>>
) {
    companion object {
        fun getMapImage(map: MapInfo) : Pair<String, File>? {
            val file = File(map.source.absoluteDir.toFile(), "map.png")
            return if (file.exists()) map.id to file else null
        }
    }

    fun buildPayload(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        for ((mapName, file) in images) {
            val nameBytes = mapName.toByteArray()
            val imageContent = file.readBytes()
            dos.writeInt(Integer.BYTES + nameBytes.size + imageContent.size)
            dos.writeInt(nameBytes.size)
            dos.write(nameBytes)
            dos.write(imageContent)
        }
        return bos.toByteArray()
    }
}
