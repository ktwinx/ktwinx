package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyObservation
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.jsonArray
import kotlin.time.Instant

class TestObservationWireContract : FunSpec({
    val hdtId = HdtId("dt-1")
    val modelName = ModelName("vitals")
    val modelId = HdtIdFactory.modelId(hdtId, modelName)

    fun obs(value: PropertyValue): PropertyObservation {
        val propertyName = PropertyName("p")
        return PropertyObservation(
            hdtId = hdtId, modelId = modelId, modelName = modelName,
            propertyId = HdtIdFactory.propertyId(modelId, propertyName),
            propertyName = propertyName,
            timestamp = Instant.parse("2026-06-08T12:00:00Z"),
            value = value,
        )
    }

    val everyValue = listOf(
        PropertyValue.IntPropertyValue(1),
        PropertyValue.LongPropertyValue(1L),
        PropertyValue.FloatPropertyValue(1.0f),
        PropertyValue.DoublePropertyValue(1.0),
        PropertyValue.StringPropertyValue("s"),
        PropertyValue.BooleanPropertyValue(true),
        PropertyValue.EmptyPropertyValue,
    )

    context("producer Stub.hdtJson -> canonical observation SerDe agree per PropertyValue subtype") {
        everyValue.forEach { v ->
            test("cross-config round-trip for ${v::class.simpleName}") {
                val wire = Stub.hdtJson.encodeToString(
                    ListSerializer(PropertyObservation.serializer()), listOf(obs(v))
                )
                val jsonArray = Stub.hdtJson.parseToJsonElement(wire).jsonArray
                val back = jsonArray.map { Stub.observationJsonSerDe().deserializeFromJsonElement(it) }
                back shouldBe listOf(obs(v))
            }
        }
    }

    context("producer Stub.hdtJson -> canonical observation SerDe agree for List<PropertyObservation>") {
        test("cross-config round-trip for List<PropertyObservation> with all subtypes") {
            val observations = everyValue.map { obs(it) }
            val wire = Stub.hdtJson.encodeToString(
                ListSerializer(PropertyObservation.serializer()), observations
            )
            val jsonArray = Stub.hdtJson.parseToJsonElement(wire).jsonArray
            val back = jsonArray.map { Stub.observationJsonSerDe().deserializeFromJsonElement(it) }
            back shouldBe observations
        }
    }

    context("producer Stub.hdtJson -> canonical HDT SerDe agree for List<HumanDigitalTwin>") {
        test("cross-config round-trip for List<HumanDigitalTwin>") {
            val hdt = HumanDigitalTwin(hdtId = hdtId, models = emptyList())
            val wire = Stub.hdtJson.encodeToString(
                ListSerializer(HumanDigitalTwin.serializer()), listOf(hdt)
            )
            val jsonArray = Stub.hdtJson.parseToJsonElement(wire).jsonArray
            val back = jsonArray.map { Stub.hdtJsonSerDe().deserializeFromJsonElement(it) }
            back shouldBe listOf(hdt)
        }
    }
})
