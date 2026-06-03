package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.WellKnownFormats
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TestTagsCodingFormat : FunSpec({

    val hdtId = HdtId("dt-1")
    val modelName = ModelName("vitals")
    val modelId = HdtIdFactory.modelId(hdtId, modelName)

    context("Property JSON round-trip with tags and coding") {
        test("round-trips non-empty tags") {
            val prop = Property(
                modelId = modelId,
                name = PropertyName("heart-rate"),
                description = PropertyDescription("Heart rate in BPM"),
                declaredType = PropertyValueType.INT,
                tags = mapOf("unit" to "bpm", "category" to "vital-signs"),
            )
            val serde = Stub.propertyJsonSerDe()
            serde.deserialize(serde.serialize(prop)) shouldBe prop
        }

        test("round-trips non-null coding") {
            val prop = Property(
                modelId = modelId,
                name = PropertyName("heart-rate"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.INT,
                coding = Coding(system = "loinc", code = "8867-4"),
            )
            val serde = Stub.propertyJsonSerDe()
            serde.deserialize(serde.serialize(prop)) shouldBe prop
        }

        test("round-trips both non-empty tags and non-null coding") {
            val prop = Property(
                modelId = modelId,
                name = PropertyName("spo2"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.FLOAT,
                tags = mapOf("unit" to "%", "system" to "respiratory"),
                coding = Coding(system = "loinc", code = "59408-5"),
            )
            val serde = Stub.propertyJsonSerDe()
            serde.deserialize(serde.serialize(prop)) shouldBe prop
        }

        test("round-trips default empty tags and null coding") {
            val prop = Property(
                modelId = modelId,
                name = PropertyName("temperature"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.DOUBLE,
            )
            val serde = Stub.propertyJsonSerDe()
            val deserialized = serde.deserialize(serde.serialize(prop))
            deserialized shouldBe prop
            deserialized.tags shouldBe emptyMap()
            deserialized.coding shouldBe null
        }
    }

    context("Model JSON round-trip with tags and format") {
        test("round-trips non-empty tags") {
            val model = Model(
                hdtId = hdtId,
                name = modelName,
                description = ModelDescription(""),
                properties = emptyList(),
                tags = mapOf("domain" to "cardiology"),
            )
            val serde = Stub.modelJsonSerDe()
            serde.deserialize(serde.serialize(model)) shouldBe model
        }

        test("round-trips non-default format") {
            val model = Model(
                hdtId = hdtId,
                name = modelName,
                description = ModelDescription(""),
                properties = emptyList(),
                format = WellKnownFormats.FHIR_R4,
            )
            val serde = Stub.modelJsonSerDe()
            serde.deserialize(serde.serialize(model)) shouldBe model
        }

        test("round-trips both non-empty tags and non-default format") {
            val model = Model(
                hdtId = hdtId,
                name = modelName,
                description = ModelDescription(""),
                properties = listOf(
                    Property(
                        modelId = modelId,
                        name = PropertyName("heart-rate"),
                        description = PropertyDescription(""),
                        declaredType = PropertyValueType.INT,
                        tags = mapOf("unit" to "bpm"),
                        coding = Coding(system = "loinc", code = "8867-4"),
                    )
                ),
                tags = mapOf("domain" to "cardiology"),
                format = WellKnownFormats.RAW,
            )
            val serde = Stub.modelJsonSerDe()
            serde.deserialize(serde.serialize(model)) shouldBe model
        }

        test("round-trips default empty tags and UNSPECIFIED format") {
            val model = Model(
                hdtId = hdtId,
                name = modelName,
                description = ModelDescription(""),
                properties = emptyList(),
            )
            val serde = Stub.modelJsonSerDe()
            val deserialized = serde.deserialize(serde.serialize(model))
            deserialized shouldBe model
            deserialized.tags shouldBe emptyMap()
            deserialized.format shouldBe WellKnownFormats.UNSPECIFIED
        }
    }

    context("HumanDigitalTwin JSON round-trip with tags") {
        test("round-trips non-empty HDT tags") {
            val hdt = HumanDigitalTwin(
                hdtId = hdtId,
                models = emptyList(),
                tags = mapOf("owner" to "alice", "project" to "preterm-2024"),
            )
            val serde = Stub.hdtJsonSerDe()
            serde.deserialize(serde.serialize(hdt)) shouldBe hdt
        }

        test("round-trips default empty HDT tags") {
            val hdt = HumanDigitalTwin(
                hdtId = hdtId,
                models = emptyList(),
            )
            val serde = Stub.hdtJsonSerDe()
            val deserialized = serde.deserialize(serde.serialize(hdt))
            deserialized shouldBe hdt
            deserialized.tags shouldBe emptyMap()
        }

        test("round-trips full HDT with models, properties, tags, coding, and format") {
            val prop = Property(
                modelId = modelId,
                name = PropertyName("heart-rate"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.INT,
                tags = mapOf("unit" to "bpm"),
                coding = Coding(system = "loinc", code = "8867-4"),
            )
            val model = Model(
                hdtId = hdtId,
                name = modelName,
                description = ModelDescription(""),
                properties = listOf(prop),
                tags = mapOf("domain" to "cardiology"),
                format = WellKnownFormats.FHIR_R4,
            )
            val hdt = HumanDigitalTwin(
                hdtId = hdtId,
                models = listOf(model),
                tags = mapOf("owner" to "alice"),
            )
            val serde = Stub.hdtJsonSerDe()
            serde.deserialize(serde.serialize(hdt)) shouldBe hdt
        }
    }
})
