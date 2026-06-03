package io.github.ktwinx.core.hdt.model.property

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull

class ToPropertyValueTest : FunSpec({

    test("null maps to EmptyPropertyValue") {
        null.toPropertyValue() shouldBe PropertyValue.EmptyPropertyValue
    }

    test("String maps to StringPropertyValue") {
        "hello".toPropertyValue() shouldBe PropertyValue.StringPropertyValue("hello")
    }

    test("Int maps to IntPropertyValue") {
        42.toPropertyValue() shouldBe PropertyValue.IntPropertyValue(42)
    }

    test("Long maps to LongPropertyValue") {
        100L.toPropertyValue() shouldBe PropertyValue.LongPropertyValue(100L)
    }

    test("Float maps to FloatPropertyValue") {
        3.14f.toPropertyValue() shouldBe PropertyValue.FloatPropertyValue(3.14f)
    }

    test("Double maps to DoublePropertyValue") {
        2.718.toPropertyValue() shouldBe PropertyValue.DoublePropertyValue(2.718)
    }

    test("Boolean true maps to BooleanPropertyValue") {
        true.toPropertyValue() shouldBe PropertyValue.BooleanPropertyValue(true)
    }

    test("Boolean false maps to BooleanPropertyValue") {
        false.toPropertyValue() shouldBe PropertyValue.BooleanPropertyValue(false)
    }

    test("unsupported type returns null") {
        listOf(1, 2, 3).toPropertyValue().shouldBeNull()
    }

    test("another unsupported type returns null") {
        mapOf("a" to 1).toPropertyValue().shouldBeNull()
    }
})
