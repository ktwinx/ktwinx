package io.github.ktwinx.core.hdt.event

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CodingLinkedUpdateEventTest : FunSpec({

    val testJson = Json {
        serializersModule = SerializersModule {
            polymorphic(PropertyValue::class) {
                subclass(PropertyValue.EmptyPropertyValue::class, PropertyValue.EmptyPropertyValue.serializer())
                subclass(PropertyValue.StringPropertyValue::class, PropertyValue.StringPropertyValue.serializer())
                subclass(PropertyValue.IntPropertyValue::class, PropertyValue.IntPropertyValue.serializer())
                subclass(PropertyValue.FloatPropertyValue::class, PropertyValue.FloatPropertyValue.serializer())
                subclass(PropertyValue.BooleanPropertyValue::class, PropertyValue.BooleanPropertyValue.serializer())
                subclass(PropertyValue.DoublePropertyValue::class, PropertyValue.DoublePropertyValue.serializer())
                subclass(PropertyValue.LongPropertyValue::class, PropertyValue.LongPropertyValue.serializer())
            }
        }
        classDiscriminator = "type"
    }

    val hdtId = HdtId("test-hdt")
    val modelId1 = HdtIdFactory.modelId(hdtId, ModelName("raw"))
    val modelId2 = HdtIdFactory.modelId(hdtId, ModelName("fhir"))
    val sourceId = HdtIdFactory.propertyId(modelId1, PropertyName("systolic"))
    val linkedId = HdtIdFactory.propertyId(modelId2, PropertyName("systolic-fhir"))
    val coding = Coding("loinc", "8480-6")

    fun roundTrip(event: CodingLinkedUpdateEvent): CodingLinkedUpdateEvent {
        val json = testJson.encodeToString(CodingLinkedUpdateEvent.serializer(), event)
        return testJson.decodeFromString(CodingLinkedUpdateEvent.serializer(), json)
    }

    test("WLDT_EVENT_KEY constant equals expected value") {
        CodingLinkedUpdateEvent.WLDT_EVENT_KEY shouldBe "whdt.coding-linked-update"
    }

    test("data class structural equality") {
        val e1 = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.IntPropertyValue(120))
        val e2 = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.IntPropertyValue(120))
        e1 shouldBe e2
    }

    test("JSON round-trip with IntPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.IntPropertyValue(120))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with StringPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.StringPropertyValue("120 mmHg"))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with LongPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.LongPropertyValue(9876543210L))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with FloatPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.FloatPropertyValue(120.5f))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with DoublePropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.DoublePropertyValue(120.567))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with BooleanPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.BooleanPropertyValue(true))
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with EmptyPropertyValue") {
        val event = CodingLinkedUpdateEvent(sourceId, coding, listOf(linkedId), PropertyValue.EmptyPropertyValue)
        roundTrip(event) shouldBe event
    }

    test("JSON round-trip with multiple linkedPropertyIds") {
        val linkedId2 = HdtIdFactory.propertyId(
            HdtIdFactory.modelId(hdtId, ModelName("hl7")),
            PropertyName("bp-systolic"),
        )
        val event = CodingLinkedUpdateEvent(
            sourceId, coding, listOf(linkedId, linkedId2), PropertyValue.IntPropertyValue(120)
        )
        roundTrip(event) shouldBe event
    }
})
