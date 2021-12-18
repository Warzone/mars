package network.warzone.mars.utils

import tc.oc.pgm.lib.net.kyori.adventure.text.TextComponent

data class MissingConfigPathException(val path: String) : RuntimeException("Missing path: $path")

abstract class FeatureException : RuntimeException() {

    abstract fun asTextComponent(): TextComponent

}