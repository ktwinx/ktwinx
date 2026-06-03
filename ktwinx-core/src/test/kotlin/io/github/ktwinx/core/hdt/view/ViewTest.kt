package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.query.eq
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class ViewTest : FunSpec({

    val json = Json { encodeDefaults = true }

    // -------------------------------------------------------------------------
    // ViewName — step 1
    // -------------------------------------------------------------------------

    context("ViewName") {
        test("accepts non-blank value") {
            ViewName("motor").value shouldBe "motor"
        }

        test("rejects blank string") {
            shouldThrow<IllegalArgumentException> { ViewName("") }
        }

        test("rejects whitespace-only string") {
            shouldThrow<IllegalArgumentException> { ViewName("   ") }
        }

        test("toString returns the raw value") {
            ViewName("motor").toString() shouldBe "motor"
        }

        test("equality via underlying value") {
            ViewName("motor") shouldBe ViewName("motor")
        }

        test("JSON round-trip") {
            val name = ViewName("motor")
            val encoded = json.encodeToString(name)
            json.decodeFromString<ViewName>(encoded) shouldBe name
        }
    }

    // -------------------------------------------------------------------------
    // View data class — step 2
    // -------------------------------------------------------------------------

    context("View") {
        test("construction with all defaults") {
            val v = View(ViewName("test"))
            v.name shouldBe ViewName("test")
            v.predicate shouldBe null
            v.groupByKeys shouldBe emptyList()
        }

        test("construction with all fields set") {
            val pred = eq("domain", "motor")
            val v = View(ViewName("motor-by-task"), pred, listOf("task", "visit"))
            v.name shouldBe ViewName("motor-by-task")
            v.predicate shouldBe pred
            v.groupByKeys shouldBe listOf("task", "visit")
        }

        test("rejects blank groupByKeys entry") {
            shouldThrow<IllegalArgumentException> { View(ViewName("v"), groupByKeys = listOf("")) }
        }

        test("rejects whitespace-only groupByKeys entry") {
            shouldThrow<IllegalArgumentException> { View(ViewName("v"), groupByKeys = listOf("task", "   ")) }
        }

        test("structural equality") {
            val v1 = View(ViewName("motor"), eq("domain", "motor"), listOf("task"))
            val v2 = View(ViewName("motor"), eq("domain", "motor"), listOf("task"))
            v1 shouldBe v2
        }

        test("JSON round-trip with defaults") {
            val v = View(ViewName("all"))
            json.decodeFromString<View>(json.encodeToString(v)) shouldBe v
        }

        test("JSON round-trip with non-null predicate and multi-key groupByKeys") {
            val v = View(ViewName("motor-by-task"), eq("domain", "motor"), listOf("task", "visit"))
            json.decodeFromString<View>(json.encodeToString(v)) shouldBe v
        }
    }
})
