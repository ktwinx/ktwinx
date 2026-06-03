package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json

class ViewResultTest : FunSpec({

    val hdtId = HdtId("test-hdt")
    val modelId: ModelId = HdtIdFactory.modelId(hdtId, ModelName("m"))

    fun prop(name: String, tags: Map<String, String> = emptyMap()) = Property(
        modelId = modelId,
        name = PropertyName(name),
        description = PropertyDescription(""),
        declaredType = PropertyValueType.STRING,
        tags = tags,
    )

    val json = Json { encodeDefaults = true }

    fun roundTrip(result: ViewResult): ViewResult =
        json.decodeFromString<ViewResult>(json.encodeToString<ViewResult>(result))

    // -------------------------------------------------------------------------
    // Structural tests — step 3
    // -------------------------------------------------------------------------

    context("ViewResult structural") {
        test("Flat can be constructed") {
            val p = prop("p1")
            val flat = ViewResult.Flat(listOf(p))
            flat.properties shouldBe listOf(p)
        }

        test("Grouped can be constructed with non-null bucket key") {
            val flat = ViewResult.Flat(listOf(prop("p1")))
            val grouped = ViewResult.Grouped("task", mapOf("grasp" to flat))
            grouped.key shouldBe "task"
            grouped.buckets["grasp"] shouldBe flat
        }

        test("Grouped allows null bucket key") {
            val flat = ViewResult.Flat(emptyList())
            val grouped = ViewResult.Grouped("task", mapOf(null to flat))
            grouped.buckets[null] shouldBe flat
        }

        test("recursive nesting is allowed by the type system") {
            val inner = ViewResult.Grouped("task", mapOf("grasp" to ViewResult.Flat(listOf(prop("p1")))))
            val outer = ViewResult.Grouped("domain", mapOf("motor" to inner))
            (outer.buckets["motor"] as ViewResult.Grouped).key shouldBe "task"
        }

        test("structural equality holds for Flat") {
            val p = prop("p1")
            ViewResult.Flat(listOf(p)) shouldBe ViewResult.Flat(listOf(p))
        }

        test("structural equality holds for Grouped") {
            val flat = ViewResult.Flat(listOf(prop("p1")))
            ViewResult.Grouped("task", mapOf("grasp" to flat)) shouldBe
                ViewResult.Grouped("task", mapOf("grasp" to flat))
        }
    }

    // -------------------------------------------------------------------------
    // JSON round-trip — step 4
    // -------------------------------------------------------------------------

    context("ViewResult JSON round-trip") {
        test("Flat round-trips") {
            val flat = ViewResult.Flat(listOf(prop("p1")))
            roundTrip(flat) shouldBe flat
        }

        test("single-level Grouped with non-null bucket key round-trips") {
            val grouped = ViewResult.Grouped(
                key = "task",
                buckets = linkedMapOf("grasp" to ViewResult.Flat(listOf(prop("p1")))),
            )
            roundTrip(grouped) shouldBe grouped
        }

        test("single-level Grouped with null bucket key round-trips") {
            val grouped = ViewResult.Grouped(
                key = "task",
                buckets = linkedMapOf(null to ViewResult.Flat(listOf(prop("p1")))),
            )
            val decoded = roundTrip(grouped)
            decoded shouldBe grouped
            (decoded as ViewResult.Grouped).buckets shouldContainKey null
        }

        test("null bucket key survives round-trip — explicit order check") {
            val grouped = ViewResult.Grouped(
                key = "domain",
                buckets = linkedMapOf(
                    "motor"  to ViewResult.Flat(listOf(prop("p1"))),
                    null     to ViewResult.Flat(listOf(prop("p2"))),
                    "sensor" to ViewResult.Flat(listOf(prop("p3"))),
                ),
            )
            val decoded = roundTrip(grouped) as ViewResult.Grouped
            decoded.buckets.keys.toList() shouldBe listOf("motor", null, "sensor")
        }

        test("3-level nested Grouped round-trips preserving insertion order at depth >= 3") {
            val p1 = prop("p1")
            val p2 = prop("p2")
            val p3 = prop("p3")
            val p4 = prop("p4")
            val level2a = ViewResult.Grouped("task", linkedMapOf(
                "grasp" to ViewResult.Flat(listOf(p1)),
                null    to ViewResult.Flat(listOf(p2)),
            ))
            val level2b = ViewResult.Grouped("task", linkedMapOf(
                "push" to ViewResult.Flat(listOf(p3)),
                "rest" to ViewResult.Flat(listOf(p4)),
            ))
            val level1 = ViewResult.Grouped("domain", linkedMapOf(
                "motor"  to level2a,
                "sensor" to level2b,
            ))

            val decoded = roundTrip(level1) as ViewResult.Grouped
            decoded shouldBe level1

            // Explicitly assert insertion order at each level
            decoded.buckets.keys.toList() shouldBe listOf("motor", "sensor")
            (decoded.buckets["motor"] as ViewResult.Grouped).buckets.keys.toList() shouldBe listOf("grasp", null)
            (decoded.buckets["sensor"] as ViewResult.Grouped).buckets.keys.toList() shouldBe listOf("push", "rest")
        }
    }

    // -------------------------------------------------------------------------
    // walkLeaves — step 5
    // -------------------------------------------------------------------------

    context("walkLeaves") {
        test("Flat invokes action once with empty path") {
            val p = prop("p1")
            val calls = mutableListOf<Pair<List<String?>, List<Property>>>()
            ViewResult.Flat(listOf(p)).walkLeaves { path, props -> calls += path to props }
            calls.size shouldBe 1
            calls[0].first shouldBe emptyList()
            calls[0].second shouldBe listOf(p)
        }

        test("2-level Grouped invokes action for each leaf with 2-element path") {
            val p1 = prop("p1")
            val p2 = prop("p2")
            val grouped = ViewResult.Grouped("domain", linkedMapOf(
                "motor"  to ViewResult.Grouped("task", linkedMapOf("grasp" to ViewResult.Flat(listOf(p1)))),
                "sensor" to ViewResult.Grouped("task", linkedMapOf("rest"  to ViewResult.Flat(listOf(p2)))),
            ))
            val calls = mutableListOf<Pair<List<String?>, List<Property>>>()
            grouped.walkLeaves { path, props -> calls += path to props }
            calls.size shouldBe 2
            calls[0].first shouldBe listOf("motor", "grasp")
            calls[0].second shouldBe listOf(p1)
            calls[1].first shouldBe listOf("sensor", "rest")
            calls[1].second shouldBe listOf(p2)
        }

        test("null bucket key appears as null in the path") {
            val p = prop("p1")
            val grouped = ViewResult.Grouped("task", linkedMapOf(null to ViewResult.Flat(listOf(p))))
            val calls = mutableListOf<Pair<List<String?>, List<Property>>>()
            grouped.walkLeaves { path, props -> calls += path to props }
            calls[0].first shouldBe listOf<String?>(null)
        }
    }

    // -------------------------------------------------------------------------
    // toFlatMap — step 5
    // -------------------------------------------------------------------------

    context("toFlatMap") {
        test("Flat returns single entry with empty path key") {
            val p = prop("p1")
            ViewResult.Flat(listOf(p)).toFlatMap() shouldBe mapOf(emptyList<String?>() to listOf(p))
        }

        test("returns same paths and properties as walkLeaves in order") {
            val p1 = prop("p1")
            val p2 = prop("p2")
            val grouped = ViewResult.Grouped("task", linkedMapOf(
                "grasp" to ViewResult.Flat(listOf(p1)),
                null    to ViewResult.Flat(listOf(p2)),
            ))
            val fromWalkLeaves = LinkedHashMap<List<String?>, List<Property>>()
            grouped.walkLeaves { path, props -> fromWalkLeaves[path] = props }
            grouped.toFlatMap() shouldBe fromWalkLeaves
        }

        test("preserves depth-first order") {
            val p1 = prop("p1")
            val p2 = prop("p2")
            val p3 = prop("p3")
            val grouped = ViewResult.Grouped("domain", linkedMapOf(
                "motor"  to ViewResult.Grouped("task", linkedMapOf(
                    "grasp" to ViewResult.Flat(listOf(p1)),
                    "push"  to ViewResult.Flat(listOf(p2)),
                )),
                "sensor" to ViewResult.Flat(listOf(p3)),
            ))
            grouped.toFlatMap().keys.toList() shouldBe listOf(
                listOf("motor", "grasp"),
                listOf("motor", "push"),
                listOf("sensor"),
            )
        }
    }
})
