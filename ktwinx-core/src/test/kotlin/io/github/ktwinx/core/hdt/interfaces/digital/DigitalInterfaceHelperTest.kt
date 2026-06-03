package io.github.ktwinx.core.hdt.interfaces.digital

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.interfaces.config.MalformedConfigValueException
import io.github.ktwinx.core.hdt.interfaces.config.MissingConfigKeyException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class DigitalInterfaceHelperTest : FunSpec({

    fun di(config: Map<String, String> = emptyMap()) = DigitalInterface(
        interfaceType = DigitalInterfaceType.MQTT,
        hdtId = HdtId("test-hdt"),
        name = DigitalInterfaceName("test-di"),
        config = config,
    )

    context("requireString") {
        test("returns value when key is present") {
            di(mapOf("host" to "broker.local")).requireString("host") shouldBe "broker.local"
        }

        test("throws MissingConfigKeyException when key is absent") {
            val ex = shouldThrow<MissingConfigKeyException> { di().requireString("host") }
            ex.message shouldContain "host"
            ex.message shouldContain "MQTT"
        }
    }

    context("requireInt") {
        test("parses a valid integer string") {
            di(mapOf("port" to "1883")).requireInt("port") shouldBe 1883
        }

        test("throws MissingConfigKeyException when key is absent") {
            shouldThrow<MissingConfigKeyException> { di().requireInt("port") }
        }

        test("throws MalformedConfigValueException for a non-integer string") {
            val ex = shouldThrow<MalformedConfigValueException> {
                di(mapOf("port" to "not-a-number")).requireInt("port")
            }
            ex.message shouldContain "port"
            ex.message shouldContain "Int"
            ex.message shouldContain "not-a-number"
        }
    }

    context("requireBoolean") {
        test("parses 'true'") {
            di(mapOf("tls" to "true")).requireBoolean("tls") shouldBe true
        }

        test("parses 'false'") {
            di(mapOf("tls" to "false")).requireBoolean("tls") shouldBe false
        }

        test("throws MissingConfigKeyException when key is absent") {
            shouldThrow<MissingConfigKeyException> { di().requireBoolean("tls") }
        }

        test("throws MalformedConfigValueException for an invalid boolean string") {
            val ex = shouldThrow<MalformedConfigValueException> {
                di(mapOf("tls" to "yes")).requireBoolean("tls")
            }
            ex.message shouldContain "tls"
            ex.message shouldContain "Boolean"
            ex.message shouldContain "yes"
        }
    }

    context("optionalString") {
        test("returns value when key is present") {
            di(mapOf("host" to "broker.local")).optionalString("host", "localhost") shouldBe "broker.local"
        }

        test("returns default when key is absent") {
            di().optionalString("host", "localhost") shouldBe "localhost"
        }
    }

    context("optionalInt") {
        test("parses and returns value when key is present") {
            di(mapOf("port" to "1883")).optionalInt("port", 9999) shouldBe 1883
        }

        test("returns default when key is absent") {
            di().optionalInt("port", 1883) shouldBe 1883
        }

        test("throws MalformedConfigValueException for a non-integer string even with default") {
            val ex = shouldThrow<MalformedConfigValueException> {
                di(mapOf("port" to "bad")).optionalInt("port", 1883)
            }
            ex.message shouldContain "port"
            ex.message shouldContain "Int"
        }
    }

    context("optionalBoolean") {
        test("parses and returns value when key is present") {
            di(mapOf("tls" to "true")).optionalBoolean("tls", false) shouldBe true
        }

        test("returns default when key is absent") {
            di().optionalBoolean("tls", false) shouldBe false
        }

        test("throws MalformedConfigValueException for an invalid boolean string even with default") {
            val ex = shouldThrow<MalformedConfigValueException> {
                di(mapOf("tls" to "1")).optionalBoolean("tls", false)
            }
            ex.message shouldContain "tls"
            ex.message shouldContain "Boolean"
        }
    }
})
