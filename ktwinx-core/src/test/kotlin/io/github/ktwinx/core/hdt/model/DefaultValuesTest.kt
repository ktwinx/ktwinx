package io.github.ktwinx.core.hdt.model

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class DefaultValuesTest : FunSpec({

    val hdtId = HdtId("dt-1")
    val modelName = ModelName("vitals")
    val modelId = HdtIdFactory.modelId(hdtId, modelName)

    fun minimalProperty() = Property(
        modelId = modelId,
        name = PropertyName("heart-rate"),
        description = PropertyDescription(""),
        declaredType = PropertyValueType.INT,
    )

    fun minimalModel() = Model(
        hdtId = hdtId,
        name = modelName,
        description = ModelDescription(""),
        properties = emptyList(),
    )

    context("Property default values") {
        test("tags defaults to emptyMap") {
            minimalProperty().tags.shouldBeEmpty()
        }

        test("coding defaults to null") {
            minimalProperty().coding shouldBe null
        }
    }

    context("Model default values") {
        test("tags defaults to emptyMap") {
            minimalModel().tags.shouldBeEmpty()
        }

        test("format defaults to WellKnownFormats.UNSPECIFIED") {
            minimalModel().format shouldBe WellKnownFormats.UNSPECIFIED
        }
    }

    context("Property with explicit tags and coding") {
        test("stores provided tags") {
            val prop = minimalProperty().copy(tags = mapOf("unit" to "bpm"))
            prop.tags shouldBe mapOf("unit" to "bpm")
        }

        test("stores provided coding") {
            val coding = Coding(system = "loinc", code = "8867-4")
            val prop = minimalProperty().copy(coding = coding)
            prop.coding shouldBe coding
        }
    }

    context("Model with explicit tags and format") {
        test("stores provided tags") {
            val model = minimalModel().copy(tags = mapOf("domain" to "cardiology"))
            model.tags shouldBe mapOf("domain" to "cardiology")
        }

        test("stores provided format") {
            val model = minimalModel().copy(format = WellKnownFormats.FHIR_R4)
            model.format shouldBe WellKnownFormats.FHIR_R4
        }
    }
})
