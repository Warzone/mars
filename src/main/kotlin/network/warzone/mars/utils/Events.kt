package network.warzone.mars.utils

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class KEvent : Event() {
    companion object {
        private val HANDLERS: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}