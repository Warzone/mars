package network.warzone.mars.map.models

import network.warzone.mars.feature.NamedResource
import java.util.*

data class GameMap (
    override val _id: UUID,
    override var name: String,
    var nameLower: String,
    var version: String,
    var gamemodes: List<String>,
    val loadedAt: Date,
    val updatedAt: Date,
    var authors: List<MapContributor>,
    var contributors: List<MapContributor>
) : NamedResource

data class MapContributor(val uuid: UUID, val contribution: String?)
