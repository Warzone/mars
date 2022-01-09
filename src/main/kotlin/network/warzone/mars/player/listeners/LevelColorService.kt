package network.warzone.mars.player.listeners

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import network.warzone.mars.api.ApiClient
import network.warzone.mars.feature.Resource
import network.warzone.mars.utils.colorFromName
import tc.oc.pgm.lib.net.kyori.adventure.text.format.NamedTextColor
import java.util.*

@Serializable
data class LevelColor(
    val level : Int,
    val color : String
)

object LevelColorService {
    private val DEFAULT_COLOR: NamedTextColor = NamedTextColor.GRAY
    private var colors : List<LevelColor>? = null

    init {
        runBlocking {
            colors = ApiClient.get<List<LevelColor>>("/mc/levels/colors").sortedBy { it.level }
        }
    }

   fun chatColorFromLevel(level : Int) : NamedTextColor {
        var color = DEFAULT_COLOR
        for (colorEntry in colors ?: emptyList()) {
            if (level >= colorEntry.level) color = colorFromName(colorEntry.color) ?: DEFAULT_COLOR
            else return color
        }
        return color
    }
}
