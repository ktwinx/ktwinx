package io.github.ktwinx.distributed.id

import kotlinx.serialization.Serializable

/**
 * Encapsulates the Sender of a [io.github.ktwinx.distributed.message.Message] in distributed contexts.
 */
@JvmInline
@Serializable
value class SenderId(val id: String) {

    companion object {
        fun of(raw: String): SenderId {
            require(raw.isNotEmpty())
            require(raw.isNotBlank())
            require(raw.length <= 64)
            return SenderId(raw)
        }
    }
}