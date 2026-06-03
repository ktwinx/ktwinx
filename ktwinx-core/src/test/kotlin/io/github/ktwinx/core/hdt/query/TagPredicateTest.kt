package io.github.ktwinx.core.hdt.query

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TagPredicateTest : FunSpec({

    // -------------------------------------------------------------------------
    // Structural tests — variant construction and data-class equality
    // -------------------------------------------------------------------------

    context("TagPredicate variants — construction and structural equality") {

        test("Eq constructs and compares by value") {
            TagPredicate.Eq("domain", "motor") shouldBe TagPredicate.Eq("domain", "motor")
        }

        test("In constructs and compares by value") {
            TagPredicate.In("env", setOf("prod", "staging")) shouldBe
                TagPredicate.In("env", setOf("staging", "prod"))
        }

        test("Has constructs and compares by value") {
            TagPredicate.Has("task") shouldBe TagPredicate.Has("task")
        }

        test("And constructs and compares by value") {
            val a = TagPredicate.And(listOf(TagPredicate.Has("x"), TagPredicate.Has("y")))
            val b = TagPredicate.And(listOf(TagPredicate.Has("x"), TagPredicate.Has("y")))
            a shouldBe b
        }

        test("Or constructs and compares by value") {
            val a = TagPredicate.Or(listOf(TagPredicate.Eq("k", "v")))
            val b = TagPredicate.Or(listOf(TagPredicate.Eq("k", "v")))
            a shouldBe b
        }

        test("Not constructs and compares by value") {
            TagPredicate.Not(TagPredicate.Has("x")) shouldBe TagPredicate.Not(TagPredicate.Has("x"))
        }

        test("sealed when expression is exhaustive over all six variants") {
            val variants: List<TagPredicate> = listOf(
                TagPredicate.Eq("k", "v"),
                TagPredicate.In("k", setOf("v")),
                TagPredicate.Has("k"),
                TagPredicate.And(emptyList()),
                TagPredicate.Or(emptyList()),
                TagPredicate.Not(TagPredicate.Has("k")),
            )
            val names = variants.map {
                when (it) {
                    is TagPredicate.Eq  -> "Eq"
                    is TagPredicate.In  -> "In"
                    is TagPredicate.Has -> "Has"
                    is TagPredicate.And -> "And"
                    is TagPredicate.Or  -> "Or"
                    is TagPredicate.Not -> "Not"
                }
            }
            names shouldBe listOf("Eq", "In", "Has", "And", "Or", "Not")
        }
    }

    // -------------------------------------------------------------------------
    // matches() — leaf operators
    // -------------------------------------------------------------------------

    val fixture = mapOf("domain" to "motor", "task" to "grasp", "env" to "prod")

    context("matches() — Eq") {
        test("returns true when tag equals expected value") {
            TagPredicate.Eq("domain", "motor").matches(fixture) shouldBe true
        }

        test("returns false when tag has a different value") {
            TagPredicate.Eq("domain", "sensor").matches(fixture) shouldBe false
        }

        test("returns false when tag is absent") {
            TagPredicate.Eq("missing", "x").matches(fixture) shouldBe false
        }
    }

    context("matches() — In") {
        test("returns true when tag value is in the set") {
            TagPredicate.In("env", setOf("prod", "staging")).matches(fixture) shouldBe true
        }

        test("returns false when tag value is not in the set") {
            TagPredicate.In("env", setOf("dev", "staging")).matches(fixture) shouldBe false
        }

        test("returns false when tag is absent") {
            TagPredicate.In("missing", setOf("a", "b")).matches(fixture) shouldBe false
        }
    }

    context("matches() — Has") {
        test("returns true when the key exists") {
            TagPredicate.Has("task").matches(fixture) shouldBe true
        }

        test("returns false when the key is absent") {
            TagPredicate.Has("missing").matches(fixture) shouldBe false
        }
    }

    context("matches() — Not") {
        test("negates a true predicate to false") {
            TagPredicate.Not(TagPredicate.Has("task")).matches(fixture) shouldBe false
        }

        test("negates a false predicate to true") {
            TagPredicate.Not(TagPredicate.Has("missing")).matches(fixture) shouldBe true
        }
    }

    context("matches() — And") {
        test("true when all terms match") {
            TagPredicate.And(listOf(
                TagPredicate.Eq("domain", "motor"),
                TagPredicate.Has("task"),
            )).matches(fixture) shouldBe true
        }

        test("false when at least one term does not match") {
            TagPredicate.And(listOf(
                TagPredicate.Eq("domain", "motor"),
                TagPredicate.Eq("task", "push"),
            )).matches(fixture) shouldBe false
        }

        test("identity law: And(emptyList()) is true") {
            TagPredicate.And(emptyList()).matches(emptyMap()) shouldBe true
        }
    }

    context("matches() — Or") {
        test("true when at least one term matches") {
            TagPredicate.Or(listOf(
                TagPredicate.Eq("domain", "sensor"),
                TagPredicate.Has("task"),
            )).matches(fixture) shouldBe true
        }

        test("false when no term matches") {
            TagPredicate.Or(listOf(
                TagPredicate.Eq("domain", "sensor"),
                TagPredicate.Has("missing"),
            )).matches(fixture) shouldBe false
        }

        test("identity law: Or(emptyList()) is false") {
            TagPredicate.Or(emptyList()).matches(emptyMap()) shouldBe false
        }
    }

    context("matches() — nested composition") {
        test("and(eq, or(has, not(has))) evaluates correctly") {
            val pred = TagPredicate.And(listOf(
                TagPredicate.Eq("domain", "motor"),
                TagPredicate.Or(listOf(
                    TagPredicate.Has("missing"),
                    TagPredicate.Not(TagPredicate.Has("missing")),
                )),
            ))
            pred.matches(fixture) shouldBe true
        }
    }
})
