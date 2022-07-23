package network.warzone.mars.utils.strategy

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import network.warzone.mars.utils.JOIN_CONFIG

fun Audience.multiLine(): MultiLineStrategy {
    return MultiLineStrategy(this)
}

class MultiLineStrategy(
    private val player: Audience
) {

    private val components =
        mutableListOf<Component>()

    fun appendMultiLine(
        component: () -> Component
    ) : MultiLineStrategy {
        this.appendMultiLineComponent(component())
        return this
    }

    fun appendMultiLineComponent(
        component: Component
    ) : MultiLineStrategy {
        this.components += component
        return this
    }

    fun append(
        vararg component: Component
    ) : MultiLineStrategy {
        this.components += Component
            .join(JOIN_CONFIG, *component)

        return this
    }

    fun deliver() {
        for (component in components)
        {
            this.player.sendMessage(component)
        }
    }
}
