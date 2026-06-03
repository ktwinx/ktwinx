package io.github.ktwinx.core.hdt.model.property

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json

class CodingTest : FunSpec({

    context("Coding construction") {
        test("accepts valid system and code") {
            val coding = Coding(system = "loinc", code = "8480-6")
            coding.system shouldBe "loinc"
            coding.code shouldBe "8480-6"
        }

        test("rejects blank system") {
            shouldThrow<IllegalArgumentException> { Coding(system = "", code = "8480-6") }
                .message shouldContain "system"
        }

        test("rejects whitespace-only system") {
            shouldThrow<IllegalArgumentException> { Coding(system = "   ", code = "8480-6") }
                .message shouldContain "system"
        }

        test("rejects blank code") {
            shouldThrow<IllegalArgumentException> { Coding(system = "loinc", code = "") }
                .message shouldContain "code"
        }

        test("rejects whitespace-only code") {
            shouldThrow<IllegalArgumentException> { Coding(system = "loinc", code = "   ") }
                .message shouldContain "code"
        }
    }

    context("Coding JSON round-trip") {
        val json = Json { encodeDefaults = true }

        test("serializes and deserializes correctly") {
            val coding = Coding(system = "loinc", code = "8480-6")
            val serialized = json.encodeToString(Coding.serializer(), coding)
            val deserialized = json.decodeFromString(Coding.serializer(), serialized)
            deserialized shouldBe coding
        }

        test("serializes system and code fields") {
            val coding = Coding(system = "snomed", code = "271649006")
            val serialized = json.encodeToString(Coding.serializer(), coding)
            serialized shouldContain "snomed"
            serialized shouldContain "271649006"
        }
    }
})
