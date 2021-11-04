package network.warzone.mars.feature.named

import network.warzone.mars.feature.resource.Resource

interface NamedResource : Resource {
    val name: String
}