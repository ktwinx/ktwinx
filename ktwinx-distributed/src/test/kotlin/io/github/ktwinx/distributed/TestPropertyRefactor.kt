package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyObservation
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.core.hdt.model.property.valueType
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TestPropertyRefactor : FunSpec({

    context("PropertyValueType.defaultFor()") {
        test("EMPTY returns EmptyPropertyValue") {
            PropertyValueType.EMPTY.defaultFor().shouldBeInstanceOf<PropertyValue.EmptyPropertyValue>()
        }
        test("STRING returns StringPropertyValue with empty string") {
            PropertyValueType.STRING.defaultFor().shouldBeInstanceOf<PropertyValue.StringPropertyValue>().value shouldBe ""
        }
        test("INT returns IntPropertyValue with 0") {
            PropertyValueType.INT.defaultFor().shouldBeInstanceOf<PropertyValue.IntPropertyValue>().value shouldBe 0
        }
        test("LONG returns LongPropertyValue with 0L") {
            PropertyValueType.LONG.defaultFor().shouldBeInstanceOf<PropertyValue.LongPropertyValue>().value shouldBe 0L
        }
        test("FLOAT returns FloatPropertyValue with 0f") {
            PropertyValueType.FLOAT.defaultFor().shouldBeInstanceOf<PropertyValue.FloatPropertyValue>().value shouldBe 0f
        }
        test("DOUBLE returns DoublePropertyValue with 0.0") {
            PropertyValueType.DOUBLE.defaultFor().shouldBeInstanceOf<PropertyValue.DoublePropertyValue>().value shouldBe 0.0
        }
        test("BOOLEAN returns BooleanPropertyValue with false") {
            PropertyValueType.BOOLEAN.defaultFor().shouldBeInstanceOf<PropertyValue.BooleanPropertyValue>().value shouldBe false
        }
    }

    context("PropertyValue.valueType()") {
        test("EmptyPropertyValue -> EMPTY") {
            PropertyValue.EmptyPropertyValue.valueType() shouldBe PropertyValueType.EMPTY
        }
        test("StringPropertyValue -> STRING") {
            PropertyValue.StringPropertyValue("x").valueType() shouldBe PropertyValueType.STRING
        }
        test("IntPropertyValue -> INT") {
            PropertyValue.IntPropertyValue(1).valueType() shouldBe PropertyValueType.INT
        }
        test("LongPropertyValue -> LONG") {
            PropertyValue.LongPropertyValue(1L).valueType() shouldBe PropertyValueType.LONG
        }
        test("FloatPropertyValue -> FLOAT") {
            PropertyValue.FloatPropertyValue(1f).valueType() shouldBe PropertyValueType.FLOAT
        }
        test("DoublePropertyValue -> DOUBLE") {
            PropertyValue.DoublePropertyValue(1.0).valueType() shouldBe PropertyValueType.DOUBLE
        }
        test("BooleanPropertyValue -> BOOLEAN") {
            PropertyValue.BooleanPropertyValue(true).valueType() shouldBe PropertyValueType.BOOLEAN
        }
        test("defaultFor round-trip: every type") {
            PropertyValueType.entries.forEach { type ->
                type.defaultFor().valueType() shouldBe type
            }
        }
    }

    context("Property.init validation") {
        val modelId = HdtIdFactory.modelId(HdtId("dt-1"), ModelName("model-1"))

        test("null initialValue always succeeds") {
            Property(
                modelId = modelId,
                name = PropertyName("prop"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.STRING,
                initialValue = null,
            )
        }

        test("matching initialValue type succeeds") {
            Property(
                modelId = modelId,
                name = PropertyName("prop"),
                description = PropertyDescription(""),
                declaredType = PropertyValueType.STRING,
                initialValue = PropertyValue.StringPropertyValue("hello"),
            )
        }

        test("mismatched initialValue type throws IllegalArgumentException") {
            shouldThrow<IllegalArgumentException> {
                Property(
                    modelId = modelId,
                    name = PropertyName("prop"),
                    description = PropertyDescription(""),
                    declaredType = PropertyValueType.INT,
                    initialValue = PropertyValue.StringPropertyValue("wrong-type"),
                )
            }
        }
    }

    context("PropertyObservation SerDe round-trip") {
        test("serializes and deserializes a PropertyObservation correctly") {
            val hdtId = HdtId("dt-1")
            val modelName = ModelName("my-model")
            val modelId = HdtIdFactory.modelId(hdtId, modelName)
            val propertyName = PropertyName("heart-rate")
            val propertyId = HdtIdFactory.propertyId(modelId, propertyName)
            val observation = PropertyObservation(
                hdtId = hdtId,
                modelId = modelId,
                modelName = modelName,
                propertyId = propertyId,
                propertyName = propertyName,
                timestamp = Clock.System.now(),
                value = PropertyValue.IntPropertyValue(72),
            )
            val serde = Stub.observationJsonSerDe()
            val serialized = serde.serialize(observation)
            val deserialized = serde.deserialize(serialized)
            deserialized shouldBe observation
        }
    }

    context("PropertyObservation.init consistency checks") {
        val hdtId = HdtId("dt-1")
        val modelName = ModelName("my-model")
        val modelId = HdtIdFactory.modelId(hdtId, modelName)
        val propertyName = PropertyName("heart-rate")
        val propertyId = HdtIdFactory.propertyId(modelId, propertyName)

        test("rejects inconsistent modelId") {
            shouldThrow<IllegalArgumentException> {
                PropertyObservation(
                    hdtId = hdtId,
                    modelId = HdtIdFactory.modelId(HdtId("other"), ModelName("other-model")),
                    modelName = modelName,
                    propertyId = propertyId,
                    propertyName = propertyName,
                    timestamp = Clock.System.now(),
                    value = PropertyValue.IntPropertyValue(0),
                )
            }
        }

        test("rejects inconsistent propertyId") {
            shouldThrow<IllegalArgumentException> {
                PropertyObservation(
                    hdtId = hdtId,
                    modelId = modelId,
                    modelName = modelName,
                    propertyId = HdtIdFactory.propertyId(modelId, PropertyName("other-prop")),
                    propertyName = propertyName,
                    timestamp = Clock.System.now(),
                    value = PropertyValue.IntPropertyValue(0),
                )
            }
        }

        test("consistent observation constructs successfully") {
            PropertyObservation(
                hdtId = hdtId,
                modelId = modelId,
                modelName = modelName,
                propertyId = propertyId,
                propertyName = propertyName,
                timestamp = Clock.System.now(),
                value = PropertyValue.IntPropertyValue(72),
            )
        }
    }
})
