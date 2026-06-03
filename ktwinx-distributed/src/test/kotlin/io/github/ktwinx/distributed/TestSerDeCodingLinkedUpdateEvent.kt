package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.event.CodingLinkedUpdateEvent
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TestSerDeCodingLinkedUpdateEvent : FunSpec({

    val serde = Stub.codingLinkedUpdateEventSerDe()

    val hdtId = HdtId("patient-1")
    val modelId1 = HdtIdFactory.modelId(hdtId, ModelName("raw"))
    val modelId2 = HdtIdFactory.modelId(hdtId, ModelName("fhir"))
    val sourceId = HdtIdFactory.propertyId(modelId1, PropertyName("systolic"))
    val linkedId = HdtIdFactory.propertyId(modelId2, PropertyName("systolic-fhir"))
    val coding = Coding("loinc", "8480-6")

    test("round-trip with IntPropertyValue") {
        val event = CodingLinkedUpdateEvent(
            sourcePropertyId = sourceId,
            coding = coding,
            linkedPropertyIds = listOf(linkedId),
            newValue = PropertyValue.IntPropertyValue(120),
        )
        serde.deserialize(serde.serialize(event)) shouldBe event
    }

    test("round-trip with StringPropertyValue") {
        val event = CodingLinkedUpdateEvent(
            sourcePropertyId = sourceId,
            coding = coding,
            linkedPropertyIds = listOf(linkedId),
            newValue = PropertyValue.StringPropertyValue("120 mmHg"),
        )
        serde.deserialize(serde.serialize(event)) shouldBe event
    }

    test("round-trip with multiple linkedPropertyIds") {
        val linkedId2 = HdtIdFactory.propertyId(
            HdtIdFactory.modelId(hdtId, ModelName("hl7")),
            PropertyName("bp-systolic"),
        )
        val event = CodingLinkedUpdateEvent(
            sourcePropertyId = sourceId,
            coding = coding,
            linkedPropertyIds = listOf(linkedId, linkedId2),
            newValue = PropertyValue.IntPropertyValue(120),
        )
        serde.deserialize(serde.serialize(event)) shouldBe event
    }
})
