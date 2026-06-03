package io.github.ktwinx.wldt.plugin.factory.physical

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class MqttPhysicalAdapterFactoryTest : FunSpec({

    val factory =
        MqttPhysicalAdapterFactory(Stub.observationJsonSerDe())

    fun pi(config: Map<String, String> = emptyMap()) = PhysicalInterface(
        interfaceType = PhysicalInterfaceType.MQTT,
        hdtId = HdtId("test-hdt"),
        name = PhysicalInterfaceName("test-mqtt-pi"),
        config = config,
    )

    context("interfaceType") {
        test("is MQTT") {
            factory.interfaceType shouldBe PhysicalInterfaceType.MQTT
        }
    }

    context("validate") {
        test("succeeds with empty config (all keys have defaults)") {
            factory.validate(pi()) shouldBeSuccess Unit
        }

        test("succeeds with valid broker and port") {
            factory.validate(pi(mapOf("broker" to "mqtt.local", "port" to "1883"))) shouldBeSuccess Unit
        }

        test("fails when port is malformed") {
            val result = factory.validate(pi(mapOf("port" to "not-a-number")))
            result shouldBeFailure { e ->
                e.message shouldContain "port"
            }
        }
    }

    context("create") {
        test("returns a MqttPhysicalAdapter with the correct id") {
            val pI = pi()
            val adapter = factory.create(pI, listOf(testModel()))
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter>()
            adapter.id shouldBe pI.id.toString()
        }

        test("applies default broker and port when config is empty") {
            val adapter = factory.create(pi(), listOf(testModel()))
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter>()
        }

        test("uses provided broker and port from config") {
            val adapter = factory.create(
                pi(mapOf("broker" to "custom.broker", "port" to "1884")),
                listOf(testModel()),
            )
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter>()
        }
    }
})

private fun testModel(): Model {
    val hdtId = HdtId("test-hdt")
    val modelName = ModelName("test-model")
    val model = Model(
        hdtId = hdtId,
        name = modelName,
        description = ModelDescription(""),
        properties = listOf(
            Property(
                modelId = io.github.ktwinx.core.hdt.HdtIdFactory.modelId(hdtId, modelName),
                name = PropertyName("test-prop"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.STRING,
            )
        ),
    )
    return model
}
