package io.github.ktwinx.distributed.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

interface SerDe<T> {
    val serializer: KSerializer<T>
    fun serialize(value: T): String
    fun serializeToJsonElement(value: T): JsonElement
    fun deserialize(data: String): T
    fun deserializeFromJsonElement(data: JsonElement): T
}

inline fun <reified T> jsonSerDe(json: Json = Json): SerDe<T> {
    val ser = json.serializersModule.serializer<T>()
    return object : SerDe<T> {
        override val serializer = ser
        override fun serialize(value: T): String = json.encodeToString(serializer, value)
        override fun serializeToJsonElement(value: T): JsonElement = json.encodeToJsonElement(serializer, value)
        override fun deserialize(data: String): T = json.decodeFromString(serializer, data)
        override fun deserializeFromJsonElement(data: JsonElement): T = json.decodeFromJsonElement(serializer, data)
    }
}
