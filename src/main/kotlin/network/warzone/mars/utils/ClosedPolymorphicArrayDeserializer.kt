package network.warzone.mars.utils

import com.google.gson.JsonDeserializer
import com.google.gson.JsonParseException
import kotlinx.serialization.SerialName
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object ClosedPolymorphicArrayDeserializer {
    fun <T : Any> createFromSealedClass(clazz: KClass<T>) : JsonDeserializer<T>? {
        if (!clazz.isSealed) return null
        val mappings = clazz.sealedSubclasses.map { subclazz ->
            val serialNameAnnotation = subclazz.findAnnotation<SerialName>()
            val serializedName = serialNameAnnotation?.value ?: subclazz.qualifiedName!!
            serializedName to subclazz.java
        }
        return JsonDeserializer<T> { jsonElement, _, ctx ->
            if (!jsonElement.isJsonArray) throw JsonParseException("Array format required")
            val arr = jsonElement.asJsonArray
            val tag = arr.get(0).asString
            val data = arr.get(1)
            val relevantMapping = mappings.firstOrNull { mapping -> mapping.first == tag }
                ?: throw JsonParseException("Invalid class tag")
            val relevantClazz = relevantMapping.second
            return@JsonDeserializer ctx.deserialize(data, relevantClazz)
        }
    }
}