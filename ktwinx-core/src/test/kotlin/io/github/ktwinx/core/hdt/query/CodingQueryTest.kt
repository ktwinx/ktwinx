package io.github.ktwinx.core.hdt.query

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class CodingQueryTest : FunSpec({

    val hdtId = HdtId("test-hdt")
    val coding1 = Coding("loinc", "8480-6")
    val coding2 = Coding("snomed", "271649006")

    fun modelId(name: String) = HdtIdFactory.modelId(hdtId, ModelName(name))

    fun prop(name: String, modelName: String, coding: Coding? = null) = Property(
        modelId = modelId(modelName),
        name = PropertyName(name),
        description = PropertyDescription(""),
        declaredType = PropertyValueType.INT,
        coding = coding,
    )

    fun model(name: String, props: List<Property>): Model = Model(
        hdtId = hdtId,
        name = ModelName(name),
        description = ModelDescription(""),
        properties = props,
    )

    fun hdt(vararg models: Model) = HumanDigitalTwin(hdtId = hdtId, models = models.toList())

    // -------------------------------------------------------------------------
    // List<Property>.findByCoding
    // -------------------------------------------------------------------------

    context("List<Property>.findByCoding") {
        test("returns empty list when input is empty") {
            emptyList<Property>().findByCoding(coding1).shouldBeEmpty()
        }

        test("returns matching properties") {
            val p1 = prop("systolic", "raw", coding1)
            val p2 = prop("systolic-fhir", "fhir", coding1)
            val p3 = prop("other", "raw", coding2)
            listOf(p1, p2, p3).findByCoding(coding1) shouldContainExactly listOf(p1, p2)
        }

        test("returns empty list when no properties match") {
            val p1 = prop("other", "raw", coding2)
            listOf(p1).findByCoding(coding1).shouldBeEmpty()
        }

        test("skips properties with null coding") {
            val p1 = prop("no-coding", "raw", null)
            listOf(p1).findByCoding(coding1).shouldBeEmpty()
        }

        test("preserves input order") {
            val p1 = prop("a", "raw", coding1)
            val p2 = prop("b", "fhir", coding1)
            listOf(p2, p1).findByCoding(coding1) shouldContainExactly listOf(p2, p1)
        }
    }

    // -------------------------------------------------------------------------
    // List<Property>.propertiesByCoding
    // -------------------------------------------------------------------------

    context("List<Property>.propertiesByCoding") {
        test("returns empty map when input is empty") {
            emptyList<Property>().propertiesByCoding().shouldBeEmpty()
        }

        test("excludes properties with null coding") {
            val p1 = prop("no-coding", "raw", null)
            listOf(p1).propertiesByCoding().shouldBeEmpty()
        }

        test("groups properties by coding") {
            val p1 = prop("systolic", "raw", coding1)
            val p2 = prop("systolic-fhir", "fhir", coding1)
            val p3 = prop("diastolic", "raw", coding2)
            val result = listOf(p1, p2, p3).propertiesByCoding()
            result[coding1] shouldContainExactly listOf(p1, p2)
            result[coding2] shouldContainExactly listOf(p3)
        }

        test("properties with null coding are absent from the map") {
            val p1 = prop("coded", "raw", coding1)
            val p2 = prop("uncoded", "raw", null)
            val result = listOf(p1, p2).propertiesByCoding()
            result.size shouldBe 1
            result[coding1] shouldContainExactly listOf(p1)
        }

        test("two properties with the same coding grouped together") {
            val p1 = prop("a", "model1", coding1)
            val p2 = prop("b", "model2", coding1)
            val result = listOf(p1, p2).propertiesByCoding()
            result[coding1] shouldContainExactly listOf(p1, p2)
        }
    }

    // -------------------------------------------------------------------------
    // HumanDigitalTwin.findByCoding / propertiesByCoding
    // -------------------------------------------------------------------------

    context("HumanDigitalTwin coding lookups") {
        val p1 = prop("systolic", "raw", coding1)
        val p2 = prop("systolic-fhir", "fhir", coding1)
        val p3 = prop("no-coding", "raw", null)

        val testHdt = hdt(
            model("raw", listOf(p1, p3)),
            model("fhir", listOf(p2)),
        )

        test("findByCoding returns both linked properties") {
            testHdt.findByCoding(coding1) shouldContainExactly listOf(p1, p2)
        }

        test("findByCoding returns empty for unknown coding") {
            testHdt.findByCoding(coding2).shouldBeEmpty()
        }

        test("propertiesByCoding groups linked properties") {
            val result = testHdt.propertiesByCoding()
            result[coding1]?.size shouldBe 2
            result[coding1] shouldContainExactly listOf(p1, p2)
        }

        test("propertiesByCoding excludes properties without coding") {
            val result = testHdt.propertiesByCoding()
            result.values.flatten().none { it == p3 } shouldBe true
        }
    }
})
