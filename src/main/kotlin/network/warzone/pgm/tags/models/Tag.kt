package network.warzone.pgm.tags.models

import network.warzone.pgm.feature.named.NamedResource
import network.warzone.pgm.feature.resource.Resource
import java.util.*

data class Tag(
    override val _id: UUID,
    override val name: String,
    var nameLower: String,
    var display: String,
    val createdAt: Date
) : NamedResource {

    override fun generate(): Resource {
        return this
    }

}
