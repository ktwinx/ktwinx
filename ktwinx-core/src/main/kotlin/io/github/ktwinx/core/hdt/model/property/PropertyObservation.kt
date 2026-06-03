package io.github.ktwinx.core.hdt.model.property

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
@SerialName("property-observation")
data class PropertyObservation(
    val hdtId: HdtId,
    val modelId: ModelId,
    val modelName: ModelName,
    val propertyId: PropertyId,
    val propertyName: PropertyName,
    val timestamp: Instant,
    val value: PropertyValue,
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(modelId == HdtIdFactory.modelId(hdtId, modelName)) {
            "PropertyObservation: modelId '$modelId' inconsistent with (hdtId='$hdtId', modelName='$modelName')"
        }
        require(propertyId == HdtIdFactory.propertyId(modelId, propertyName)) {
            "PropertyObservation: propertyId '$propertyId' inconsistent with (modelId='$modelId', propertyName='$propertyName')"
        }
    }

    companion object {
        fun of(
            hdt: HumanDigitalTwin,
            model: Model,
            property: Property,
            timestamp: Instant,
            value: PropertyValue,
            metadata: Map<String, String> = emptyMap(),
        ): PropertyObservation = PropertyObservation(
            hdtId = hdt.hdtId,
            modelId = model.id,
            modelName = model.name,
            propertyId = property.id,
            propertyName = property.name,
            timestamp = timestamp,
            value = value,
            metadata = metadata,
        )
    }
}
