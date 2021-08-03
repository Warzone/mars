package network.warzone.pgm.api.socket

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.bukkit.event.Event
import kotlin.reflect.full.isSubclassOf

@Serializable
data class ComplexType(val test: String)

sealed class InboundEvent<T>(val eventName: String, val toBukkit: (T) -> Event, val parse: (JsonElement) -> T) {
    object PING : InboundEvent<ComplexType>("PING", ::PingEvent, Json::decodeFromJsonElement)

    fun call(d: JsonElement) {
        toBukkit(parse(d)).callEvent()
    }
    
    companion object {
        private val map = InboundEvent::class.nestedClasses
            .filter { clazz -> clazz.isSubclassOf(InboundEvent::class) }
            .map { clazz -> clazz.objectInstance }
            .map { clazz -> clazz as InboundEvent<*> }
            .associateBy { value -> value.eventName }

        fun <T> valueOf(eventName: String): InboundEvent<T> {
            if (map.containsKey(eventName)) return map[eventName] as InboundEvent<T>

            throw IllegalArgumentException("No InboundEvent exists by the name $eventName")
        }
    }
}

class PingEvent(val data: ComplexType) : Event()