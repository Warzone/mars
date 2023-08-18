package network.warzone.mars.utils

import com.google.gson.*
import kotlinx.serialization.SerialName
import network.warzone.mars.api.socket.models.PlayerUpdateData
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object ClosedPolymorphismDeserializer {
    fun <T : Any> createFromSealedClass(clazz: KClass<T>) : JsonDeserializer<T>? {
        if (!clazz.isSealed) return null
        val mappings = clazz.sealedSubclasses.map { subclazz ->
            val serialNameAnnotation = subclazz.findAnnotation<SerialName>()
            val serializedName = serialNameAnnotation?.value ?: subclazz.qualifiedName!!
            serializedName to subclazz.java
        }
        return JsonDeserializer<T> { jsonElement, _, ctx ->
            val tag : String
            val data: JsonElement

            if (jsonElement.isJsonObject) {
                val obj = jsonElement.asJsonObject
                tag = obj.get("type").asString
                obj.remove("type")
                data = obj
            } else if (jsonElement.isJsonArray) {
                val arr = jsonElement.asJsonArray
                tag = arr.get(0).asString
                data = arr.get(1)
            } else {
                throw JsonParseException("Unknown format, original payload: $jsonElement")
            }
            val relevantMapping = mappings.firstOrNull { mapping -> mapping.first == tag }
                ?: throw JsonParseException("Invalid class tag")
            val relevantClazz = relevantMapping.second
            return@JsonDeserializer ctx.deserialize(data, relevantClazz)
        }
    }
}

private fun deepCopy(jsonObject: JsonObject): JsonObject {
    val result = JsonObject()
    for (entry in jsonObject.entrySet()) {
        if (entry.value.isJsonObject) {
            result.add(entry.key, deepCopy(entry.value.asJsonObject))
        } else if (entry.value.isJsonArray) {
            result.add(entry.key, deepCopy(entry.value.asJsonArray))
        } else if (entry.value.isJsonPrimitive) {
            result.add(entry.key, entry.value)
        }
    }
    return result
}

private fun deepCopy(jsonArray: JsonArray): JsonArray {
    val result = JsonArray()
    for (jsonElement in jsonArray) {
        if (jsonElement.isJsonObject) {
            result.add(deepCopy(jsonElement.asJsonObject))
        } else if (jsonElement.isJsonArray) {
            result.add(deepCopy(jsonElement.asJsonArray))
        } else if (jsonElement.isJsonPrimitive) {
            result.add(jsonElement)
        }
    }
    return result
}