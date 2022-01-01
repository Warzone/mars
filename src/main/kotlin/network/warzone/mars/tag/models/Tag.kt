package network.warzone.mars.tag.models

import network.warzone.mars.feature.named.NamedResource
import network.warzone.mars.feature.resource.Resource
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
