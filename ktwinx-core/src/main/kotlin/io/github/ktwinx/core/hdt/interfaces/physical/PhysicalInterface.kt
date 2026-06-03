package io.github.ktwinx.core.hdt.interfaces.physical

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.interfaces.config.MalformedConfigValueException
import io.github.ktwinx.core.hdt.interfaces.config.MissingConfigKeyException
import kotlinx.serialization.Serializable

@Serializable
enum class PhysicalInterfaceType {
    MQTT,
}

@JvmInline @Serializable value class PhysicalInterfaceId(val value: String) {
    override fun toString(): String = value
}

@JvmInline @Serializable value class PhysicalInterfaceName(val value: String) {
    init {
        require(value.isNotBlank()) { "PhysicalInterfaceName must not be blank" }
        require(':' !in value) { "PhysicalInterfaceName must not contain ':'" }
    }

    override fun toString(): String = value
}

@Serializable
data class PhysicalInterface(
    val interfaceType: PhysicalInterfaceType,
    val hdtId: HdtId,
    val name: PhysicalInterfaceName,
    val config: Map<String, String> = emptyMap(),
) {
    val id: PhysicalInterfaceId = HdtIdFactory.physicalInterfaceId(hdtId, name)

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
