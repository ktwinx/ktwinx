package io.github.ktwinx.wldt.plugin.factory.physical

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.wldt.adapter.physical.PhysicalAdapter

class PhysicalAdapterRegistryTest : FunSpec({

    fun pi(
        name: String = "test-pi",
        config: Map<String, String> = emptyMap(),
    ) = PhysicalInterface(
        interfaceType = PhysicalInterfaceType.MQTT,
        hdtId = HdtId("test-hdt"),
        name = PhysicalInterfaceName(name),
        config = config,
    )

    val alwaysOkFactory = object : PhysicalAdapterFactory {
        override val interfaceType = PhysicalInterfaceType.MQTT
        override fun validate(pI: PhysicalInterface): Result<Unit> = Result.success(Unit)
        override fun create(pI: PhysicalInterface, models: List<Model>): PhysicalAdapter =
            error("not used in registry tests")
    }

    val alwaysFailFactory = object : PhysicalAdapterFactory {
        override val interfaceType = PhysicalInterfaceType.MQTT
        override fun validate(pI: PhysicalInterface): Result<Unit> =
            Result.failure(IllegalArgumentException("bad config for ${pI.id}"))
        override fun create(pI: PhysicalInterface, models: List<Model>): PhysicalAdapter =
            error("not used in registry tests")
    }

    context("validateAll") {
        test("returns success for empty interface list") {
            val registry = PhysicalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            registry.validateAll(emptyList()) shouldBeSuccess Unit
        }

        test("returns success when all interfaces validate") {
            val registry = PhysicalAdapterRegistry(
                listOf(alwaysOkFactory)
            )
            registry.validateAll(listOf(pi())) shouldBeSuccess Unit
        }

        test("returns failure for a single failing interface") {
            val registry = PhysicalAdapterRegistry(
                listOf(alwaysFailFactory)
            )
            val result = registry.validateAll(listOf(pi()))
            result shouldBeFailure { e ->
                e.message shouldContain "bad config"
            }
        }

        test("aggregates multiple errors without short-circuiting") {
            val registry = PhysicalAdapterRegistry(
                listOf(alwaysFailFactory)
            )
            val interfaces = listOf(pi("pi-1"), pi("pi-2"))
            val result = registry.validateAll(interfaces)
            result shouldBeFailure { e ->
                e.message shouldContain "pi-1"
                e.message shouldContain "pi-2"
            }
        }

        test("returns failure for unknown interface type — registry has no factory") {
            val registry =
                PhysicalAdapterRegistry(emptyList())
            val result = registry.validateAll(listOf(pi()))
            result shouldBeFailure { e ->
                e.message shouldContain "no factory registered"
                e.message shouldContain "MQTT"
            }
        }
    }

    context("create") {
        test("returns null for unknown interface type") {
            val registry =
                PhysicalAdapterRegistry(emptyList())
            registry.create(pi(), emptyList()) shouldBe null
        }

        test("delegates to the matching factory") {
            val capturingFactory = object : PhysicalAdapterFactory {
                var called = false
                override val interfaceType = PhysicalInterfaceType.MQTT
                override fun validate(pI: PhysicalInterface): Result<Unit> = Result.success(Unit)
                override fun create(pI: PhysicalInterface, models: List<Model>): PhysicalAdapter {
                    called = true
                    error("sentinel — not a real adapter")
                }
            }
            val registry = PhysicalAdapterRegistry(
                listOf(capturingFactory)
            )
            shouldThrow<IllegalStateException> {
                registry.create(pi(), emptyList())
            }
            capturingFactory.called shouldBe true
        }
    }
})
