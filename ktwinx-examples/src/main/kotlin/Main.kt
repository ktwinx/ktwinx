package io.github.ktwinx

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.wldt.plugin.execution.WldtApp

fun main() {
    val hdtId = HdtId("Mimosa_1")
    val modelId = ModelId("$hdtId:my-model")
    val properties = listOf(
        testProperty(modelId, "First Name", PropertyValueType.STRING, PropertyValue.StringPropertyValue("John")),
        testProperty(modelId, "Surname", PropertyValueType.STRING, PropertyValue.StringPropertyValue("Doe"))
    )
    val model = Model(hdtId, ModelName("my-model"), ModelDescription("Test Model"), properties)

    val pI = PhysicalInterface(
        interfaceType = PhysicalInterfaceType.MQTT,
        hdtId = hdtId,
        name = PhysicalInterfaceName("mqtt-physical-int"),
    )

    val dI = DigitalInterface(
        interfaceType = DigitalInterfaceType.MQTT,
        hdtId = hdtId,
        name = DigitalInterfaceName("mqtt-digital-int"),
    )

    val httpDI = DigitalInterface(
        interfaceType = DigitalInterfaceType.HTTP,
        hdtId = hdtId,
        name = DigitalInterfaceName("http-digital-int"),
    )

    val hdts = listOf(
        HumanDigitalTwin(
            hdtId = hdtId,
            models = listOf(model),
            physicalInterfaces = listOf(pI),
            digitalInterfaces = listOf(dI, httpDI),
        )
    )

    val startedDts = WldtApp().addStartAll(hdts)
    println("Started Dts: ${startedDts.map { it.getOrNull() }}")
}

fun testProperty(modelId: ModelId, name: String, declaredType: PropertyValueType, initialValue: PropertyValue? = null): Property {
    return Property(
        modelId = modelId,
        name = PropertyName(name),
        description = PropertyDescription(""),
        declaredType = declaredType,
        initialValue = initialValue,
    )
}
