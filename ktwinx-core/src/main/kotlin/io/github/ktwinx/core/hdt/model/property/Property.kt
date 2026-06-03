package io.github.ktwinx.core.hdt.model.property

import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.ModelId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline @Serializable value class PropertyId(val value: String) {
    override fun toString(): String = value
}

@JvmInline @Serializable value class PropertyName(val value: String) {
    init {
        require(value.isNotBlank()) { "PropertyName must not be blank" }
        require(':' !in value) { "PropertyName must not contain ':'" }
    }

    override fun toString(): String = value
}

@JvmInline @Serializable value class PropertyDescription(val value: String) {
    override fun toString(): String = value
}

@Serializable
@SerialName("property")
data class Property(
    val modelId: ModelId,
    val name: PropertyName,
    val description: PropertyDescription,
    val declaredType: PropertyValueType,
    val initialValue: PropertyValue? = null,
    val tags: Map<String, String> = emptyMap(),
    val coding: Coding? = null,
) {
    val id: PropertyId = HdtIdFactory.propertyId(modelId, name)

    init {
        if (initialValue != null) {
            require(initialValue.valueType() == declaredType) {
                "Property '$id': initialValue type ${initialValue.valueType()} does not match declaredType $declaredType"
            }
        }
    }
}