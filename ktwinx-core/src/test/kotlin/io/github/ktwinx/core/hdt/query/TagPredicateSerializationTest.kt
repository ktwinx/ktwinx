package io.github.ktwinx.core.hdt.query

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class TagPredicateSerializationTest : FunSpec({

    val json = Json {
        classDiscriminator = "type"
        encodeDefaults = true
    }

    fun roundTrip(predicate: TagPredicate): TagPredicate {
        val encoded = json.encodeToString(TagPredicate.serializer(), predicate)
        return json.decodeFromString(TagPredicate.serializer(), encoded)
    }

    context("JSON round-trip — leaf variants") {

        test("Eq round-trips") {
            val p = TagPredicate.Eq("domain", "motor")
            roundTrip(p) shouldBe p
        }

        test("In round-trips with multiple values") {
            val p = TagPredicate.In("env", setOf("prod", "staging", "dev"))
            roundTrip(p) shouldBe p
        }

        test("Has round-trips") {
            val p = TagPredicate.Has("task")
            roundTrip(p) shouldBe p
        }

        test("And with empty list round-trips") {
            val p = TagPredicate.And(emptyList())
            roundTrip(p) shouldBe p
        }

        test("Or with empty list round-trips") {
            val p = TagPredicate.Or(emptyList())
            roundTrip(p) shouldBe p
        }

        test("Not round-trips") {
            val p = TagPredicate.Not(TagPredicate.Has("missing"))
            roundTrip(p) shouldBe p
        }
    }

    context("JSON round-trip — nested composition") {

        test("3-level nested And(Or(Not(...), Eq(...)), Has(...)) round-trips") {
            val p = TagPredicate.And(listOf(
                TagPredicate.Or(listOf(
                    TagPredicate.Not(TagPredicate.Eq("env", "prod")),
                    TagPredicate.Eq("domain", "motor"),
                )),
                TagPredicate.Has("task"),
            ))
            roundTrip(p) shouldBe p
        }
    }

    context("JSON shape — type discriminator field") {

        test("Eq encodes with type=eq") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.Eq("k", "v"))
            (encoded.contains("\"type\"") && encoded.contains("\"eq\"")) shouldBe true
        }

        test("In encodes with type=in") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.In("k", setOf("v")))
            (encoded.contains("\"type\"") && encoded.contains("\"in\"")) shouldBe true
        }

        test("Has encodes with type=has") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.Has("k"))
            (encoded.contains("\"type\"") && encoded.contains("\"has\"")) shouldBe true
        }

        test("And encodes with type=and") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.And(emptyList()))
            (encoded.contains("\"type\"") && encoded.contains("\"and\"")) shouldBe true
        }

        test("Or encodes with type=or") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.Or(emptyList()))
            (encoded.contains("\"type\"") && encoded.contains("\"or\"")) shouldBe true
        }

        test("Not encodes with type=not") {
            val encoded = json.encodeToString(TagPredicate.serializer(), TagPredicate.Not(TagPredicate.Has("k")))
            (encoded.contains("\"type\"") && encoded.contains("\"not\"")) shouldBe true
        }
    }
})
