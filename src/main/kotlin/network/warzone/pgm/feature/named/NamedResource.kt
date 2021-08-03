package network.warzone.pgm.feature.named

import network.warzone.pgm.feature.resource.Resource

interface NamedResource : Resource {
    val name: String
}