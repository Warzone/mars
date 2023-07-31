package network.warzone.mars.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.tinder.scarlet.Message
import com.tinder.scarlet.MessageAdapter
import network.warzone.api.database.models.AgentParams
import network.warzone.mars.api.socket.models.PlayerUpdateData
import okio.Buffer
import java.io.OutputStreamWriter
import java.io.StringReader
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

val GSON_CFG : GsonBuilder.() -> Unit = {
    registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
        Date(json.asJsonPrimitive.asLong)
    })
    registerTypeAdapter(Date::class.java, JsonSerializer<Date> { date, _, _ ->
        JsonPrimitive(date.time)
    })
    registerTypeAdapter(
        AgentParams::class.java,
        ClosedPolymorphismDeserializer.createFromSealedClass(AgentParams::class)
    )
    registerTypeAdapter(
        PlayerUpdateData::class.java,
        ClosedPolymorphismDeserializer.createFromSealedClass(PlayerUpdateData::class)
    )
}

val GSON = run {
    val gsonBuilder = GsonBuilder()
    gsonBuilder.GSON_CFG()
    gsonBuilder.create()!!
}

/**
 * A [message adapter][MessageAdapter] that uses Gson.
 */
class GsonMessageAdapter<T> private constructor(
    private val gson: Gson,
    private val typeAdapter: TypeAdapter<T>
) : MessageAdapter<T> {

    override fun fromMessage(message: Message): T {
        val stringValue = when (message) {
            is Message.Text -> message.value
            is Message.Bytes -> message.value.zlibDecompress()
        }
        val jsonReader = JsonReader(StringReader(stringValue))
        return typeAdapter.read(jsonReader)!!
    }

    override fun toMessage(data: T): Message {
        val buffer = Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter = JsonWriter(writer)
        typeAdapter.write(jsonWriter, data)
        jsonWriter.close()
        val stringValue = buffer.readByteString().utf8()
        return Message.Bytes(stringValue.zlibCompress())
    }

    class Factory(
        private val gson: Gson = GSON
    ) : MessageAdapter.Factory {

        override fun create(type: Type, annotations: Array<Annotation>): MessageAdapter<*> {
            val typeAdapter = gson.getAdapter(TypeToken.get(type))
            return GsonMessageAdapter(gson, typeAdapter)
        }

        companion object {
            private val DEFAULT_GSON = Gson()
        }
    }

    inline fun <reified T> fromJson(json: String): T? {
        return GSON.fromJson(json, object : TypeToken<T>() {}.type)
    }

    fun <T> mapToObject(map: Map<String, Any?>?, type: Class<T>): T? {
        if (map == null) return null

        val json = GSON.toJson(map)
        return GSON.fromJson(json, type)
    }
}