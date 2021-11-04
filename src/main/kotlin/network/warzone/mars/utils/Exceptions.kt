package network.warzone.mars.utils

import net.kyori.adventure.text.TextComponent

data class MissingConfigPathException(val path: String) : RuntimeException("Missing path: $path")

abstract class FeatureException : RuntimeException() {

    abstract fun asTextComponent(): TextComponent

}