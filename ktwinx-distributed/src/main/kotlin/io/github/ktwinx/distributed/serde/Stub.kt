package io.github.ktwinx.distributed.serde

import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.event.CodingLinkedUpdateEvent
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyObservation
import io.github.ktwinx.distributed.serde.modules.hdtModule
import io.github.ktwinx.distributed.serde.modules.interfaceModule
import io.github.ktwinx.distributed.serde.modules.propertyModule
import io.github.ktwinx.distributed.message.Message
import kotlinx.serialization.json.Json

object Stub {
    val hdtJson = Json {
        serializersModule = hdtModule
        classDiscriminator = "type"
        prettyPrint = true
        encodeDefaults = true
    }

    val propertyJson = Json {
        serializersModule = propertyModule
        classDiscriminator = "type"
        prettyPrint = true
        encodeDefaults = true
    }

    val interfaceJson = Json {
        serializersModule = interfaceModule
        classDiscriminator = "type"
        prettyPrint = true
        encodeDefaults = true
    }

    val messageJson = Json {
        prettyPrint = true
    }
    
    fun hdtJsonSerDe(): SerDe<HumanDigitalTwin> = jsonSerDe<HumanDigitalTwin>(hdtJson)
    fun propertyJsonSerDe(): SerDe<Property> = jsonSerDe<Property>(propertyJson)
    fun observationJsonSerDe(): SerDe<PropertyObservation> = jsonSerDe<PropertyObservation>(propertyJson)
    fun modelJsonSerDe(): SerDe<Model> = jsonSerDe<Model>(propertyJson)
    fun physicalInterfaceJsonSerDe(): SerDe<PhysicalInterface> = jsonSerDe<PhysicalInterface>(interfaceJson)
    fun digitalInterfaceJsonSerDe(): SerDe<DigitalInterface> = jsonSerDe<DigitalInterface>(interfaceJson)
    fun messageJsonSerDe(): SerDe<Message> = jsonSerDe<Message>(messageJson)
    fun codingLinkedUpdateEventSerDe(): SerDe<CodingLinkedUpdateEvent> = jsonSerDe<CodingLinkedUpdateEvent>(propertyJson)
}