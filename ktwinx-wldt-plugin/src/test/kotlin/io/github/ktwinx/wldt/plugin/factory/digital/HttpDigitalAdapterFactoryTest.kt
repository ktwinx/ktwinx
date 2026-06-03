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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class HttpDigitalAdapterFactoryTest : FunSpec({

    val factory = HttpDigitalAdapterFactory()

    fun di(config: Map<String, String> = emptyMap()) = DigitalInterface(
        interfaceType = DigitalInterfaceType.HTTP,
        hdtId = HdtId("test-hdt"),
        name = DigitalInterfaceName("test-http-di"),
        config = config,
    )

    context("interfaceType") {
        test("is HTTP") {
            factory.interfaceType shouldBe DigitalInterfaceType.HTTP
        }
    }

    context("validate") {
        test("succeeds with empty config (all keys have defaults)") {
            factory.validate(di()) shouldBeSuccess Unit
        }

        test("succeeds with valid host and port") {
            factory.validate(di(mapOf("host" to "api.local", "port" to "8080"))) shouldBeSuccess Unit
        }

        test("fails when port is malformed") {
            val result = factory.validate(di(mapOf("port" to "not-a-number")))
            result shouldBeFailure { e ->
                e.message shouldContain "port"
            }
        }
    }

    context("create") {
        test("returns an HttpDigitalAdapter") {
            val dI = di()
            val dt = mockDigitalTwin()
            val adapter = factory.create(dI, dt, listOf(testModel()))
            adapter.shouldBeInstanceOf<it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter>()
        }

        test("applies default host and port when config is empty") {
            val adapter = factory.create(di(),
                mockDigitalTwin(), listOf(
                    testModel()
                ))
            adapter.shouldBeInstanceOf<it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter>()
        }

        test("uses provided host and port from config") {
            val adapter = factory.create(
                di(mapOf("host" to "custom.host", "port" to "9090")),
                mockDigitalTwin(),
                listOf(testModel()),
            )
            adapter.shouldBeInstanceOf<it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter>()
        }
    }
})

private fun mockDigitalTwin() = it.wldt.core.engine.DigitalTwin(
    "mock-dt",
    _root_ide_package_.io.github.ktwinx.wldt.plugin.shadowing.KtwinxShadowingFunction("mock-sf", emptyList()),
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
