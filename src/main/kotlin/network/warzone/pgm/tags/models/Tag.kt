package network.warzone.pgm.tags.models

import network.warzone.pgm.feature.named.NamedResource
import network.warzone.pgm.feature.resource.Resource
import java.util.*

data class Tag(
    override val _id: UUID,
    override var name: String,
    var display: String,
    val createdAt: Date
) : NamedResource {

    val nameLower: String get() = name.toLowerCase(Locale.ROOT)

    override fun generate(): Resource {
        return this
    }

}
