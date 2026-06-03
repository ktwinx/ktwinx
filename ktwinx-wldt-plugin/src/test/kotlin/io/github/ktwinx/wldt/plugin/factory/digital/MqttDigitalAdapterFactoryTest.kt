package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.distributed.serde.Stub
import io.github.ktwinx.wldt.plugin.shadowing.KtwinxShadowingFunction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class MqttDigitalAdapterFactoryTest : FunSpec({

    val factory =
        MqttDigitalAdapterFactory(Stub.observationJsonSerDe())

    fun di(config: Map<String, String> = emptyMap()) = DigitalInterface(
        interfaceType = DigitalInterfaceType.MQTT,
        hdtId = HdtId("test-hdt"),
        name = DigitalInterfaceName("test-mqtt-di"),
        config = config,
    )

    context("interfaceType") {
        test("is MQTT") {
            factory.interfaceType shouldBe DigitalInterfaceType.MQTT
        }
    }

    context("validate") {
        test("succeeds with empty config (all keys have defaults)") {
            factory.validate(di()) shouldBeSuccess Unit
        }

        test("succeeds with valid broker and port") {
            factory.validate(di(mapOf("broker" to "mqtt.local", "port" to "1883"))) shouldBeSuccess Unit
        }

        test("fails when port is malformed") {
            val result = factory.validate(di(mapOf("port" to "not-a-number")))
            result shouldBeFailure { e ->
                e.message shouldContain "port"
            }
        }

        test("fails when broker key is present but empty string is accepted") {
            factory.validate(di(mapOf("broker" to ""))) shouldBeSuccess Unit
        }
    }

    context("create") {
        test("returns a MqttDigitalAdapter with the correct id") {
            val dI = di()
            val adapter = factory.create(dI,
                mockDigitalTwin(), listOf(
                    testModel()
                ))
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.digital.MqttDigitalAdapter>()
            adapter.id shouldBe dI.id.toString()
        }

        test("applies default broker and port when config is empty") {
            val adapter = factory.create(di(),
                mockDigitalTwin(), listOf(
                    testModel()
                ))
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.digital.MqttDigitalAdapter>()
        }

        test("uses provided broker and port from config") {
            val adapter = factory.create(
                di(mapOf("broker" to "custom.broker", "port" to "1884")),
                mockDigitalTwin(),
                listOf(testModel()),
            )
            adapter.shouldBeInstanceOf<it.wldt.adapter.mqtt.digital.MqttDigitalAdapter>()
        }
    }
})

private fun mockDigitalTwin() = it.wldt.core.engine.DigitalTwin(
    "mock-dt",
    KtwinxShadowingFunction("mock-sf", emptyList()),
)

private fun testModel(): Model {
    val hdtId = HdtId("test-hdt")
    val modelName = ModelName("test-model")
    return Model(
        hdtId = hdtId,
        name = modelName,
        description = ModelDescription(""),
        properties = listOf(
            Property(
                modelId = HdtIdFactory.modelId(hdtId, modelName),
                name = PropertyName("test-prop"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.STRING,
            )
        ),
    )
}
