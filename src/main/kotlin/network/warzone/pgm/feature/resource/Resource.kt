package network.warzone.pgm.feature.resource

import java.util.*

interface Resource {
    val _id: UUID
    fun generate(): Resource
}