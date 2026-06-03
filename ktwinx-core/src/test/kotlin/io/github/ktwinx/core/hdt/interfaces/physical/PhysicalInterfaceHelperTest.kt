package io.github.ktwinx.core.hdt.interfaces.physical

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.interfaces.config.MalformedConfigValueException
import io.github.ktwinx.core.hdt.interfaces.config.MissingConfigKeyException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class PhysicalInterfaceHelperTest : FunSpec({

    fun pi(config: Map<String, String> = emptyMap()) = PhysicalInterface(
        interfaceType = PhysicalInterfaceType.MQTT,
        hdtId = HdtId("test-hdt"),
        name = PhysicalInterfaceName("test-pi"),
        config = config,
    )

    context("requireString") {
        test("returns value when key is present") {
            pi(mapOf("broker" to "mqtt.local")).requireString("broker") shouldBe "mqtt.local"
        }

        test("throws MissingConfigKeyException when key is absent") {
            val ex = shouldThrow<MissingConfigKeyException> { pi().requireString("broker") }
            ex.message shouldContain "broker"
            ex.message shouldContain "MQTT"
        }
    }

    context("requireInt") {
        test("parses a valid integer string") {
            pi(mapOf("port" to "1883")).requireInt("port") shouldBe 1883
        }

        test("throws MissingConfigKeyException when key is absent") {
            shouldThrow<MissingConfigKeyException> { pi().requireInt("port") }
        }

        test("throws MalformedConfigValueException for a non-integer string") {
            val ex = shouldThrow<MalformedConfigValueException> {
                pi(mapOf("port" to "not-a-number")).requireInt("port")
            }
            ex.message shouldContain "port"
            ex.message shouldContain "Int"
            ex.message shouldContain "not-a-number"
        }
    }

    context("requireBoolean") {
        test("parses 'true'") {
            pi(mapOf("tls" to "true")).requireBoolean("tls") shouldBe true
        }

        test("parses 'false'") {
            pi(mapOf("tls" to "false")).requireBoolean("tls") shouldBe false
        }

        test("throws MissingConfigKeyException when key is absent") {
            shouldThrow<MissingConfigKeyException> { pi().requireBoolean("tls") }
        }

        test("throws MalformedConfigValueException for an invalid boolean string") {
            val ex = shouldThrow<MalformedConfigValueException> {
                pi(mapOf("tls" to "yes")).requireBoolean("tls")
            }
            ex.message shouldContain "tls"
            ex.message shouldContain "Boolean"
            ex.message shouldContain "yes"
        }
    }

    context("optionalString") {
        test("returns value when key is present") {
            pi(mapOf("broker" to "mqtt.local")).optionalString("broker", "localhost") shouldBe "mqtt.local"
        }

        test("returns default when key is absent") {
            pi().optionalString("broker", "localhost") shouldBe "localhost"
        }
    }

    context("optionalInt") {
        test("parses and returns value when key is present") {
            pi(mapOf("port" to "1883")).optionalInt("port", 9999) shouldBe 1883
        }

        test("returns default when key is absent") {
            pi().optionalInt("port", 1883) shouldBe 1883
        }

        test("throws MalformedConfigValueException for a non-integer string even with default") {
            val ex = shouldThrow<MalformedConfigValueException> {
                pi(mapOf("port" to "bad")).optionalInt("port", 1883)
            }
            ex.message shouldContain "port"
            ex.message shouldContain "Int"
        }
    }

    context("optionalBoolean") {
        test("parses and returns value when key is present") {
            pi(mapOf("tls" to "true")).optionalBoolean("tls", false) shouldBe true
        }

        test("returns default when key is absent") {
            pi().optionalBoolean("tls", false) shouldBe false
        }

        test("throws MalformedConfigValueException for an invalid boolean string even with default") {
            val ex = shouldThrow<MalformedConfigValueException> {
                pi(mapOf("tls" to "1")).optionalBoolean("tls", false)
            }
            ex.message shouldContain "tls"
            ex.message shouldContain "Boolean"
        }
    }
})
