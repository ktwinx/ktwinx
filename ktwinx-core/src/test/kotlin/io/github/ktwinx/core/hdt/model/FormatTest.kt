package io.github.ktwinx.core.hdt.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

class FormatTest : FunSpec({

    context("Format construction") {
        test("accepts a non-blank value") {
            Format("fhir-r4").value shouldBe "fhir-r4"
        }

        test("toString returns the underlying value") {
            Format("custom").toString() shouldBe "custom"
        }

        test("rejects blank value") {
            shouldThrow<IllegalArgumentException> { Format("") }
                .message shouldContain "blank"
        }

        test("rejects whitespace-only value") {
            shouldThrow<IllegalArgumentException> { Format("   ") }
                .message shouldContain "blank"
        }

        test("equality is value-based") {
            Format("raw") shouldBe Format("raw")
        }
    }

    context("WellKnownFormats") {
        val constants = listOf(
            WellKnownFormats.UNSPECIFIED,
            WellKnownFormats.RAW,
            WellKnownFormats.CUSTOM,
            WellKnownFormats.FHIR_R4,
        )

        test("all four constants are non-blank") {
            constants.forEach { format ->
                format.value.isNotBlank() shouldBe true
            }
        }

        test("all four constants are distinct") {
            constants.map { it.value }.toSet() shouldHaveSize 4
        }

        test("UNSPECIFIED value is 'unspecified'") {
            WellKnownFormats.UNSPECIFIED.value shouldBe "unspecified"
        }

        test("RAW value is 'raw'") {
            WellKnownFormats.RAW.value shouldBe "raw"
        }

        test("CUSTOM value is 'custom'") {
            WellKnownFormats.CUSTOM.value shouldBe "custom"
        }

        test("FHIR_R4 value is 'fhir-r4'") {
            WellKnownFormats.FHIR_R4.value shouldBe "fhir-r4"
        }
    }

    context("Format JSON round-trip") {
        val json = Json { encodeDefaults = true }

        test("serializes and deserializes correctly") {
            val format = WellKnownFormats.FHIR_R4
            val serialized = json.encodeToString(Format.serializer(), format)
            val deserialized = json.decodeFromString(Format.serializer(), serialized)
            deserialized shouldBe format
        }

        test("serializes as the underlying string value") {
            val serialized = json.encodeToString(Format.serializer(), WellKnownFormats.RAW)
            serialized shouldContain "raw"
        }
    }
})
