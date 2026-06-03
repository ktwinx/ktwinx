package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.wldt.adapter.digital.DigitalAdapter
import it.wldt.core.engine.DigitalTwin

class DigitalAdapterRegistryTest : FunSpec({

    fun di(
        type: DigitalInterfaceType,
        name: String = "test-di",
        config: Map<String, String> = emptyMap(),
    ) = DigitalInterface(
        interfaceType = type,
        hdtId = HdtId("test-hdt"),
        name = DigitalInterfaceName(name),
        config = config,
    )

    val alwaysOkFactory = object : DigitalAdapterFactory {
        override val interfaceType = DigitalInterfaceType.MQTT
        override fun validate(dI: DigitalInterface): Result<Unit> = Result.success(Unit)
        override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*> =
            error("not used in registry tests")
    }

    val alwaysFailFactory = object : DigitalAdapterFactory {
        override val interfaceType = DigitalInterfaceType.HTTP
        override fun validate(dI: DigitalInterface): Result<Unit> =
            Result.failure(IllegalArgumentException("bad config for ${dI.id}"))
        override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*> =
            error("not used in registry tests")
    }

    context("validateAll") {
        test("returns success for empty interface list") {
            val registry = DigitalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            registry.validateAll(emptyList()) shouldBeSuccess Unit
        }

        test("returns success when all interfaces validate") {
            val registry = DigitalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            registry.validateAll(listOf(di(DigitalInterfaceType.MQTT))) shouldBeSuccess Unit
        }

        test("returns failure for a single failing interface") {
            val registry = DigitalAdapterRegistry(
                listOf(
                    alwaysOkFactory,
                    alwaysFailFactory
                )
            )
            val result = registry.validateAll(listOf(di(DigitalInterfaceType.HTTP)))
            result shouldBeFailure { e ->
                e.message shouldContain "bad config"
            }
        }

        test("aggregates multiple errors without short-circuiting") {
            val registry = DigitalAdapterRegistry(
                listOf(
                    alwaysOkFactory,
                    alwaysFailFactory
                )
            )
            val interfaces = listOf(
                di(DigitalInterfaceType.HTTP, "di-1"),
                di(DigitalInterfaceType.HTTP, "di-2"),
            )
            val result = registry.validateAll(interfaces)
            result shouldBeFailure { e ->
                e.message shouldContain "di-1"
                e.message shouldContain "di-2"
            }
        }

        test("aggregates errors from different factories") {
            val anotherFailFactory = object : DigitalAdapterFactory {
                override val interfaceType = DigitalInterfaceType.MQTT
                override fun validate(dI: DigitalInterface): Result<Unit> =
                    Result.failure(IllegalArgumentException("mqtt fail for ${dI.id}"))
                override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*> =
                    error("not used")
            }
            val registry = DigitalAdapterRegistry(
                listOf(
                    anotherFailFactory,
                    alwaysFailFactory
                )
            )
            val interfaces = listOf(
                di(DigitalInterfaceType.MQTT, "di-mqtt"),
                di(DigitalInterfaceType.HTTP, "di-http"),
            )
            val result = registry.validateAll(interfaces)
            result shouldBeFailure { e ->
                e.message shouldContain "di-mqtt"
                e.message shouldContain "di-http"
            }
        }

        test("returns failure for unknown interface type") {
            val registry = DigitalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            val result = registry.validateAll(listOf(di(DigitalInterfaceType.HTTP)))
            result shouldBeFailure { e ->
                e.message shouldContain "no factory registered"
                e.message shouldContain "HTTP"
            }
        }
    }

    context("create") {
        test("returns null for unknown interface type") {
            val registry = DigitalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            registry.create(di(DigitalInterfaceType.HTTP),
                mockDigitalTwin(), emptyList()) shouldBe null
        }

        test("delegates to the matching factory") {
            val capturingFactory = object : DigitalAdapterFactory {
                var called = false
                override val interfaceType = DigitalInterfaceType.MQTT
                override fun validate(dI: DigitalInterface): Result<Unit> = Result.success(Unit)
                override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*> {
                    called = true
                    error("sentinel — not a real adapter") // won't reach assertion check
                }
            }
            val registry = DigitalAdapterRegistry(
                listOf(capturingFactory)
            )
            shouldThrow<IllegalStateException> {
                registry.create(di(DigitalInterfaceType.MQTT),
                    mockDigitalTwin(), emptyList())
            }
            capturingFactory.called shouldBe true
        }
    }
})

private fun mockDigitalTwin(): DigitalTwin = DigitalTwin(
    "mock-dt",
    _root_ide_package_.io.github.ktwinx.wldt.plugin.shadowing.KtwinxShadowingFunction("mock-sf", emptyList()),
)
