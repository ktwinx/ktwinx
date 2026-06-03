package io.github.ktwinx.distributed.message

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.distributed.id.SenderId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Encapsulates a message regarding a specific [io.github.ktwinx.core.hdt.HumanDigitalTwin] in distributed contexts.
 */
@Serializable
@OptIn(ExperimentalTime::class)
data class Message(
    val hdt: HdtId,
    val sender: SenderId,
    val sentAt: Long,
    @Transient
    val receivedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val payload: JsonElement,
) {

    /**
     * @param T the expected type for payload
     * @return the Message's deserialized payload, wrapped in a [Result]
     */
    inline fun<reified T> unwrap(): Result<T> {
        return runCatching {
            Json.decodeFromJsonElement(payload)
        }
    }
}
