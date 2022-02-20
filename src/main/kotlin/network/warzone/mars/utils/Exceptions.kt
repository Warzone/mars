package network.warzone.mars.utils

import tc.oc.pgm.lib.net.kyori.adventure.text.Component

data class MissingConfigPathException(val path: String) : RuntimeException("Missing path: $path")

abstract class FeatureException : RuntimeException() {
    abstract fun asComponent(): Component
}