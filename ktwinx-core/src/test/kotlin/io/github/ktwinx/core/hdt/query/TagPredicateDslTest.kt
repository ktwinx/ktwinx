package io.github.ktwinx.core.hdt.query

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TagPredicateDslTest : FunSpec({

    context("DSL builders — produce expected ADT shapes") {

        test("eq produces Eq") {
            eq("domain", "motor") shouldBe TagPredicate.Eq("domain", "motor")
        }

        test("inSet vararg produces In with a set") {
            inSet("env", "prod", "staging") shouldBe TagPredicate.In("env", setOf("prod", "staging"))
        }

        test("inSet Set overload produces In") {
            inSet("env", setOf("prod", "staging")) shouldBe TagPredicate.In("env", setOf("prod", "staging"))
        }

        test("inSet vararg and Set overload produce equivalent results") {
            val vararg = inSet("k", "a", "b", "c")
            val set    = inSet("k", setOf("a", "b", "c"))
            vararg shouldBe set
        }

        test("has produces Has") {
            has("task") shouldBe TagPredicate.Has("task")
        }

        test("not wraps a predicate in Not") {
            not(has("task")) shouldBe TagPredicate.Not(TagPredicate.Has("task"))
        }

        test("and vararg produces And") {
            and(eq("a", "b"), has("c")) shouldBe
                TagPredicate.And(listOf(TagPredicate.Eq("a", "b"), TagPredicate.Has("c")))
        }

        test("and List overload produces And") {
            and(listOf(eq("a", "b"), has("c"))) shouldBe
                TagPredicate.And(listOf(TagPredicate.Eq("a", "b"), TagPredicate.Has("c")))
        }

        test("and vararg and List overload produce equivalent results") {
            val vararg = and(eq("x", "1"), has("y"))
            val list   = and(listOf(eq("x", "1"), has("y")))
            vararg shouldBe list
        }

        test("or vararg produces Or") {
            or(eq("a", "b"), has("c")) shouldBe
                TagPredicate.Or(listOf(TagPredicate.Eq("a", "b"), TagPredicate.Has("c")))
        }

        test("or List overload produces Or") {
            or(listOf(eq("a", "b"), has("c"))) shouldBe
                TagPredicate.Or(listOf(TagPredicate.Eq("a", "b"), TagPredicate.Has("c")))
        }

        test("or vararg and List overload produce equivalent results") {
            val vararg = or(eq("x", "1"), has("y"))
            val list   = or(listOf(eq("x", "1"), has("y")))
            vararg shouldBe list
        }

        test("DSL functions compose into nested structures") {
            val pred = and(eq("domain", "motor"), or(has("task"), not(eq("env", "prod"))))
            pred shouldBe TagPredicate.And(listOf(
                TagPredicate.Eq("domain", "motor"),
                TagPredicate.Or(listOf(
                    TagPredicate.Has("task"),
                    TagPredicate.Not(TagPredicate.Eq("env", "prod")),
                )),
            ))
        }
    }
})
