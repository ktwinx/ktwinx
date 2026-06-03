package io.github.ktwinx.core.hdt.query

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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class PropertyQueryTest : FunSpec({

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    val hdtId = HdtId("test-hdt")

    fun modelId(name: String): ModelId = HdtIdFactory.modelId(hdtId, ModelName(name))

    fun prop(name: String, modelId: ModelId, tags: Map<String, String> = emptyMap()) = Property(
        modelId = modelId,
        name = PropertyName(name),
        description = PropertyDescription(""),
        declaredType = PropertyValueType.STRING,
        tags = tags,
    )

    fun model(name: String, props: List<Property>): Model {
        val mId = modelId(name)
        return Model(
            hdtId = hdtId,
            name = ModelName(name),
            description = ModelDescription(""),
            properties = props,
        )
    }

    fun hdt(vararg models: Model) = HumanDigitalTwin(hdtId = hdtId, models = models.toList())

    // -------------------------------------------------------------------------
    // filterByTags
    // -------------------------------------------------------------------------

    context("filterByTags") {
        val mId = modelId("m")

        test("empty input returns empty list") {
            emptyList<Property>().filterByTags(eq("domain", "motor")) shouldBe emptyList()
        }

        test("no matches returns empty list") {
            val props = listOf(prop("p1", mId, mapOf("domain" to "sensor")))
            props.filterByTags(eq("domain", "motor")) shouldBe emptyList()
        }

        test("single matching property is returned") {
            val p = prop("p1", mId, mapOf("domain" to "motor"))
            listOf(p).filterByTags(eq("domain", "motor")) shouldBe listOf(p)
        }

        test("all matching properties are returned") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor"))
            listOf(p1, p2).filterByTags(eq("domain", "motor")) shouldBe listOf(p1, p2)
        }

        test("preserves input order") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor", "task" to "grasp"))
            val p2 = prop("p2", mId, mapOf("domain" to "sensor"))
            val p3 = prop("p3", mId, mapOf("domain" to "motor", "task" to "push"))
            val result = listOf(p1, p2, p3).filterByTags(has("task"))
            result shouldBe listOf(p1, p3)
        }

        test("leaves non-matching items out") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, mapOf("domain" to "sensor"))
            val result = listOf(p1, p2).filterByTags(eq("domain", "motor"))
            result shouldContainExactly listOf(p1)
        }

        test("accepts predicates built via DSL") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor", "task" to "grasp"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor"))
            val p3 = prop("p3", mId, mapOf("domain" to "sensor"))
            val result = listOf(p1, p2, p3).filterByTags(and(eq("domain", "motor"), has("task")))
            result shouldBe listOf(p1)
        }
    }

    // -------------------------------------------------------------------------
    // groupByTag
    // -------------------------------------------------------------------------

    context("groupByTag") {
        val mId = modelId("m")

        test("empty input returns empty map") {
            emptyList<Property>().groupByTag("domain").shouldBeEmpty()
        }

        test("properties with the key are bucketed by value") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, mapOf("domain" to "sensor"))
            val result = listOf(p1, p2).groupByTag("domain")
            result["motor"] shouldBe listOf(p1)
            result["sensor"] shouldBe listOf(p2)
        }

        test("properties without the key go to the null bucket") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, emptyMap())
            val result = listOf(p1, p2).groupByTag("domain")
            result[null] shouldBe listOf(p2)
        }

        test("group keys appear in first-encounter (insertion) order") {
            val p1 = prop("p1", mId, mapOf("domain" to "sensor"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor"))
            val p3 = prop("p3", mId, mapOf("domain" to "sensor"))
            val result = listOf(p1, p2, p3).groupByTag("domain")
            result.keys.toList() shouldBe listOf("sensor", "motor")
        }

        test("properties within each bucket preserve input order") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor"))
            val p3 = prop("p3", mId, mapOf("domain" to "motor"))
            val result = listOf(p1, p2, p3).groupByTag("domain")
            result["motor"] shouldBe listOf(p1, p2, p3)
        }

        test("null bucket and named buckets can coexist") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, emptyMap())
            val result = listOf(p1, p2).groupByTag("domain")
            result.keys.toList() shouldBe listOf("motor", null)
        }
    }

    // -------------------------------------------------------------------------
    // thenGroupByTag
    // -------------------------------------------------------------------------

    context("thenGroupByTag") {
        val mId = modelId("m")

        test("produces Map<String?, Map<String?, List<Property>>>") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor", "task" to "grasp"))
            val result = listOf(p1).groupByTag("domain").thenGroupByTag("task")
            result["motor"]?.get("grasp") shouldBe listOf(p1)
        }

        test("both levels preserve insertion order") {
            val p1 = prop("p1", mId, mapOf("domain" to "sensor", "task" to "read"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor", "task" to "grasp"))
            val p3 = prop("p3", mId, mapOf("domain" to "sensor", "task" to "write"))
            val result = listOf(p1, p2, p3).groupByTag("domain").thenGroupByTag("task")
            result.keys.toList() shouldBe listOf("sensor", "motor")
            result["sensor"]?.keys?.toList() shouldBe listOf("read", "write")
        }

        test("both levels can independently have a null bucket") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor"))
            val p2 = prop("p2", mId, emptyMap())
            val result = listOf(p1, p2).groupByTag("domain").thenGroupByTag("task")
            result["motor"]?.get(null) shouldBe listOf(p1)
            result[null]?.get(null) shouldBe listOf(p2)
        }

        test("chaining produces correct inner grouping") {
            val p1 = prop("p1", mId, mapOf("domain" to "motor", "task" to "grasp"))
            val p2 = prop("p2", mId, mapOf("domain" to "motor", "task" to "push"))
            val p3 = prop("p3", mId, mapOf("domain" to "sensor"))
            val result = listOf(p1, p2, p3).groupByTag("domain").thenGroupByTag("task")
            result["motor"]?.keys?.toList() shouldBe listOf("grasp", "push")
            result["sensor"]?.get(null) shouldBe listOf(p3)
        }
    }

    // -------------------------------------------------------------------------
    // HumanDigitalTwin.allProperties()
    // -------------------------------------------------------------------------

    context("HumanDigitalTwin.allProperties()") {

        test("empty HDT returns empty list") {
            hdt().allProperties() shouldBe emptyList()
        }

        test("single-model HDT returns its properties") {
            val mId = modelId("vitals")
            val p1 = prop("heart-rate", mId)
            val p2 = prop("spo2", mId)
            val result = hdt(model("vitals", listOf(p1, p2))).allProperties()
            result shouldBe listOf(p1, p2)
        }

        test("multi-model HDT flattens in model order") {
            val mId1 = modelId("vitals")
            val mId2 = modelId("activity")
            val p1 = prop("heart-rate", mId1)
            val p2 = prop("spo2", mId1)
            val p3 = prop("steps", mId2)
            val result = hdt(model("vitals", listOf(p1, p2)), model("activity", listOf(p3))).allProperties()
            result shouldBe listOf(p1, p2, p3)
        }

        test("each property retains its modelId") {
            val mId1 = modelId("vitals")
            val mId2 = modelId("activity")
            val p1 = prop("heart-rate", mId1)
            val p2 = prop("steps", mId2)
            val allProps = hdt(model("vitals", listOf(p1)), model("activity", listOf(p2))).allProperties()
            allProps[0].modelId shouldBe mId1
            allProps[1].modelId shouldBe mId2
        }
    }

    // -------------------------------------------------------------------------
    // Integration test — preterm-neuropathology scenario (step 10)
    // -------------------------------------------------------------------------

    context("Integration — motor+task filter with groupByTag") {
        val motorMId  = modelId("motor")
        val sensorMId = modelId("sensor")

        val grasp    = prop("grasp-force",     motorMId,  mapOf("domain" to "motor",  "task"  to "grasp",  "visit" to "v1"))
        val push     = prop("push-torque",     motorMId,  mapOf("domain" to "motor",  "task"  to "push",   "visit" to "v1"))
        val emg      = prop("emg-amplitude",   motorMId,  mapOf("domain" to "motor",  "task"  to "grasp",  "visit" to "v2"))
        val eeg      = prop("eeg-band-power",  sensorMId, mapOf("domain" to "sensor", "visit" to "v1"))
        val heartRate = prop("heart-rate",     sensorMId, mapOf("domain" to "sensor", "task"  to "rest"))
        val steps    = prop("step-count",      sensorMId, mapOf("domain" to "sensor"))

        val testHdt  = hdt(
            model("motor",  listOf(grasp, push, emg)),
            model("sensor", listOf(eeg, heartRate, steps)),
        )

        test("allProperties flattens 6 properties in model order") {
            testHdt.allProperties() shouldBe listOf(grasp, push, emg, eeg, heartRate, steps)
        }

        test("filterByTags(and(eq(domain,motor), has(task))) keeps only motor+task props") {
            val result = testHdt.allProperties()
                .filterByTags(and(eq("domain", "motor"), has("task")))
            result shouldBe listOf(grasp, push, emg)
        }

        test("groupByTag(task) on motor+task props produces expected structure") {
            val grouped = testHdt.allProperties()
                .filterByTags(and(eq("domain", "motor"), has("task")))
                .groupByTag("task")
            grouped["grasp"] shouldBe listOf(grasp, emg)
            grouped["push"]  shouldBe listOf(push)
        }

        test("groupByTag(domain).thenGroupByTag(task) produces two-level hierarchy") {
            val result = testHdt.allProperties()
                .groupByTag("domain")
                .thenGroupByTag("task")
            result["motor"]?.keys?.toList()  shouldBe listOf("grasp", "push")
            result["sensor"]?.keys?.toList() shouldBe listOf(null, "rest")
        }

        test("thenGroupByTag sensor null bucket contains steps") {
            val result = testHdt.allProperties()
                .groupByTag("domain")
                .thenGroupByTag("task")
            result["sensor"]?.get(null) shouldBe listOf(eeg, steps)
        }
    }
})
