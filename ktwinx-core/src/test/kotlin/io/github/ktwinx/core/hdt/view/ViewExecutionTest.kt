package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.core.hdt.query.eq
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe

class ViewExecutionTest : FunSpec({

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    val hdtId1 = HdtId("hdt-1")
    val hdtId2 = HdtId("hdt-2")

    fun modelId(hdtId: HdtId, name: String): ModelId = HdtIdFactory.modelId(hdtId, ModelName(name))

    fun prop(name: String, modelId: ModelId, tags: Map<String, String> = emptyMap()) = Property(
        modelId = modelId,
        name = PropertyName(name),
        description = PropertyDescription(""),
        declaredType = PropertyValueType.STRING,
        tags = tags,
    )

    fun model(hdtId: HdtId, name: String, props: List<Property>) = Model(
        hdtId = hdtId,
        name = ModelName(name),
        description = ModelDescription(""),
        properties = props,
    )

    fun hdt(hdtId: HdtId, vararg models: Model) = HumanDigitalTwin(hdtId = hdtId, models = models.toList())

    // HDT 1: two motor props + one sensor prop
    val m1Motor  = modelId(hdtId1, "motor")
    val m1Sensor = modelId(hdtId1, "sensor")
    val grasp1   = prop("grasp-force", m1Motor,  mapOf("domain" to "motor",  "task" to "grasp"))
    val push1    = prop("push-torque", m1Motor,  mapOf("domain" to "motor",  "task" to "push"))
    val eeg1     = prop("eeg-band",    m1Sensor, mapOf("domain" to "sensor"))
    val hdt1     = hdt(
        hdtId1,
        model(hdtId1, "motor",  listOf(grasp1, push1)),
        model(hdtId1, "sensor", listOf(eeg1)),
    )

    // HDT 2: motor props (one without task) + one sensor prop
    val m2Motor  = modelId(hdtId2, "motor")
    val m2Sensor = modelId(hdtId2, "sensor")
    val grasp2   = prop("grasp-force", m2Motor,  mapOf("domain" to "motor",  "task" to "grasp"))
    val rest2    = prop("rest-torque", m2Motor,  mapOf("domain" to "motor"))   // no task tag
    val hr2      = prop("heart-rate",  m2Sensor, mapOf("domain" to "sensor", "task" to "rest"))
    val hdt2     = hdt(
        hdtId2,
        model(hdtId2, "motor",  listOf(grasp2, rest2)),
        model(hdtId2, "sensor", listOf(hr2)),
    )

    // -------------------------------------------------------------------------
    // execute(List<Property>) — step 7
    // -------------------------------------------------------------------------

    context("execute(List<Property>)") {
        test("no predicate no groupByKeys returns Flat of all input") {
            val view = View(ViewName("all"))
            view.execute(listOf(grasp1, push1)) shouldBe ViewResult.Flat(listOf(grasp1, push1))
        }

        test("predicate-only View returns Flat of filtered properties") {
            val view = View(ViewName("motor"), eq("domain", "motor"))
            view.execute(listOf(grasp1, push1, eeg1)) shouldBe ViewResult.Flat(listOf(grasp1, push1))
        }

        test("one-key groupByKeys returns Grouped with correct buckets") {
            val view = View(ViewName("by-task"), groupByKeys = listOf("task"))
            val result = view.execute(listOf(grasp1, push1)) as ViewResult.Grouped
            result.key shouldBe "task"
            result.buckets["grasp"] shouldBe ViewResult.Flat(listOf(grasp1))
            result.buckets["push"]  shouldBe ViewResult.Flat(listOf(push1))
        }

        test("null bucket holds properties missing the group-by key") {
            val view = View(ViewName("by-task"), groupByKeys = listOf("task"))
            val result = view.execute(listOf(grasp1, eeg1)) as ViewResult.Grouped
            result.buckets["grasp"] shouldBe ViewResult.Flat(listOf(grasp1))
            result.buckets[null]    shouldBe ViewResult.Flat(listOf(eeg1))
        }

        test("two-key groupByKeys returns nested Grouped") {
            val view = View(ViewName("two-key"), groupByKeys = listOf("domain", "task"))
            val result = view.execute(listOf(grasp1, push1, eeg1)) as ViewResult.Grouped
            result.key shouldBe "domain"
            val motorBucket  = result.buckets["motor"]  as ViewResult.Grouped
            val sensorBucket = result.buckets["sensor"] as ViewResult.Grouped
            motorBucket.key shouldBe "task"
            motorBucket.buckets["grasp"] shouldBe ViewResult.Flat(listOf(grasp1))
            motorBucket.buckets["push"]  shouldBe ViewResult.Flat(listOf(push1))
            sensorBucket.key shouldBe "task"
            sensorBucket.buckets[null] shouldBe ViewResult.Flat(listOf(eeg1))
        }
    }

    // -------------------------------------------------------------------------
    // execute(Model) — step 8
    // -------------------------------------------------------------------------

    context("execute(Model)") {
        test("delegates to execute(model.properties)") {
            val view = View(ViewName("motor"), eq("domain", "motor"))
            val motorModel = hdt1.models.first { it.name == ModelName("motor") }
            view.execute(motorModel) shouldBe view.execute(motorModel.properties)
        }
    }

    // -------------------------------------------------------------------------
    // execute(HumanDigitalTwin) — step 8
    // -------------------------------------------------------------------------

    context("execute(HumanDigitalTwin)") {
        test("delegates to execute(hdt.allProperties())") {
            val view = View(ViewName("motor"), eq("domain", "motor"))
            view.execute(hdt1) shouldBe view.execute(listOf(grasp1, push1, eeg1))
        }
    }

    // -------------------------------------------------------------------------
    // execute(List<HumanDigitalTwin>) — step 9
    // -------------------------------------------------------------------------

    context("execute(List<HumanDigitalTwin>)") {
        val motorView = View(ViewName("motor"), eq("domain", "motor"))

        test("empty population returns empty map") {
            motorView.execute(emptyList<HumanDigitalTwin>()).shouldBeEmpty()
        }

        test("population result is keyed by HdtId in input order") {
            val result = motorView.execute(listOf(hdt1, hdt2))
            result.keys.toList() shouldBe listOf(hdtId1, hdtId2)
        }

        test("each entry is the View result for that HDT independently") {
            val result = motorView.execute(listOf(hdt1, hdt2))
            result[hdtId1] shouldBe motorView.execute(hdt1)
            result[hdtId2] shouldBe motorView.execute(hdt2)
        }
    }

    // -------------------------------------------------------------------------
    // Integration test — preterm-style population scenario — step 10
    // -------------------------------------------------------------------------

    context("Integration — population-scale preterm scenario") {
        val motorByTask = View(
            name = ViewName("motor-by-task"),
            predicate = eq("domain", "motor"),
            groupByKeys = listOf("task"),
        )

        test("result map contains both HDT IDs") {
            val result = motorByTask.execute(listOf(hdt1, hdt2))
            result shouldContainKey hdtId1
            result shouldContainKey hdtId2
        }

        test("each value is a Grouped keyed on task") {
            val result = motorByTask.execute(listOf(hdt1, hdt2))
            (result[hdtId1] as? ViewResult.Grouped)?.key shouldBe "task"
            (result[hdtId2] as? ViewResult.Grouped)?.key shouldBe "task"
        }

        test("hdt1 bucket counts: grasp and push buckets, no null bucket") {
            val grouped = motorByTask.execute(listOf(hdt1, hdt2))[hdtId1] as ViewResult.Grouped
            grouped.buckets["grasp"] shouldBe ViewResult.Flat(listOf(grasp1))
            grouped.buckets["push"]  shouldBe ViewResult.Flat(listOf(push1))
            grouped.buckets.keys.toList() shouldBe listOf("grasp", "push")
        }

        test("hdt2 has null bucket for motor prop without task") {
            val grouped = motorByTask.execute(listOf(hdt1, hdt2))[hdtId2] as ViewResult.Grouped
            grouped.buckets["grasp"] shouldBe ViewResult.Flat(listOf(grasp2))
            grouped.buckets[null]    shouldBe ViewResult.Flat(listOf(rest2))
        }

        test("walkLeaves over hdt1 result yields expected paths") {
            val grouped = motorByTask.execute(listOf(hdt1, hdt2))[hdtId1] as ViewResult.Grouped
            val leaves = mutableListOf<Pair<List<String?>, List<Property>>>()
            grouped.walkLeaves { path, props -> leaves += path to props }
            leaves shouldBe listOf(
                listOf("grasp") to listOf(grasp1),
                listOf("push")  to listOf(push1),
            )
        }
    }
})
