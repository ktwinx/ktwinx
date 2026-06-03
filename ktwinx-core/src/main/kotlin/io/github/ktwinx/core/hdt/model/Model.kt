package io.github.ktwinx.core.hdt.model

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.property.Property
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline @Serializable value class ModelId(val value: String) {
    override fun toString(): String = value
}

@JvmInline @Serializable value class ModelName(val value: String) {
    init {
        require(value.isNotBlank()) { "ModelName must not be blank" }
        require(':' !in value) { "ModelName must not contain ':'" }
    }

    override fun toString(): String = value
}

@JvmInline @Serializable value class ModelDescription(val value: String) {
    override fun toString(): String = value
}

@Serializable
@SerialName("model")
data class Model(
    val hdtId: HdtId,
    val name: ModelName,
    val description: ModelDescription,
    val properties: List<Property>,
    val tags: Map<String, String> = emptyMap(),
    val format: Format = WellKnownFormats.UNSPECIFIED,
) {
    val id: ModelId = HdtIdFactory.modelId(hdtId, name)

    init {
        val dupProperties = properties.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(dupProperties.isEmpty()) { "Duplicate property IDs in model '$id': $dupProperties" }
        require(properties.all { it.modelId == id }) {
            "All properties must reference model '$id'; mismatched: ${properties.filter { it.modelId != id }.map { it.id }}"
        }
    }
}
