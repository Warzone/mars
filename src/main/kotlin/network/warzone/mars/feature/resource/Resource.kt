package network.warzone.mars.feature.resource

import java.util.*

interface Resource {
    val _id: UUID
    fun generate(): Resource
}