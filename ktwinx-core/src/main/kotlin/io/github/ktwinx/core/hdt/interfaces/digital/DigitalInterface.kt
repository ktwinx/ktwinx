package io.github.ktwinx.core.hdt.interfaces.digital

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.interfaces.config.MalformedConfigValueException
import io.github.ktwinx.core.hdt.interfaces.config.MissingConfigKeyException
import kotlinx.serialization.Serializable

@Serializable
enum class DigitalInterfaceType {
    MQTT,
    HTTP,
}

@JvmInline @Serializable value class DigitalInterfaceId(val value: String) {
    override fun toString(): String = value
}

@JvmInline @Serializable value class DigitalInterfaceName(val value: String) {
    init {
        require(value.isNotBlank()) { "DigitalInterfaceName must not be blank" }
        require(':' !in value) { "DigitalInterfaceName must not contain ':'" }
    }

    override fun toString(): String = value
}

@Serializable
data class DigitalInterface(
    val interfaceType: DigitalInterfaceType,
    val hdtId: HdtId,
    val name: DigitalInterfaceName,
    val config: Map<String, String> = emptyMap(),
) {
    val id: DigitalInterfaceId = HdtIdFactory.digitalInterfaceId(hdtId, name)

    fun requireString(key: String): String =
        config[key] ?: throw MissingConfigKeyException(key, interfaceType.toString())

    fun requireInt(key: String): Int = requireString(key).let {
        it.toIntOrNull() ?: throw MalformedConfigValueException(key, "Int", it)
    }

    fun requireBoolean(key: String): Boolean = requireString(key).let {
        it.toBooleanStrictOrNull() ?: throw MalformedConfigValueException(key, "Boolean", it)
    }

    fun optionalString(key: String, default: String): String = config[key] ?: default

    fun optionalInt(key: String, default: Int): Int =
        config[key]?.let { it.toIntOrNull() ?: throw MalformedConfigValueException(key, "Int", it) }
            ?: default

    fun optionalBoolean(key: String, default: Boolean): Boolean =
        config[key]?.let { it.toBooleanStrictOrNull() ?: throw MalformedConfigValueException(key, "Boolean", it) }
            ?: default
}
