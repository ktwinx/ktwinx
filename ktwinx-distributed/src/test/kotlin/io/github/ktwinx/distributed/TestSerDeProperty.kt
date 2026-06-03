package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelDescription
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.core.hdt.model.property.PropertyValue.Companion.pv
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TestSerDeProperty: FunSpec({
    test("Test SerDe GenericProperty") {
        val serde = Stub.propertyJsonSerDe()
        val modelId = ModelId("my-model")
        val prop = Property(
            modelId,
            name = PropertyName("username"),
            description = PropertyDescription("The username of the user."),
            declaredType = PropertyValueType.STRING,
            initialValue = PropertyValue.StringPropertyValue("leona"),
        )
        val serialized = serde.serialize(prop)
        val deserialized = serde.deserialize(serialized)

        deserialized shouldBe prop
    }

    test("Test SerDe Model") {
        val serde = Stub.modelJsonSerDe()
        val hdtId = HdtId("dt-1")
        val modelName = ModelName("my-model")
        val modelId = HdtIdFactory.modelId(hdtId, modelName)
        val model = Model(
            hdtId,
            modelName,
            ModelDescription("Test Model"),
            listOf(
                buildProperty(modelId, "username", PropertyValueType.STRING, "leona".pv()),
                buildProperty(modelId, "password", PropertyValueType.STRING, "123456".pv()),
            )
        )
        val serialized = serde.serialize(model)
        val deserialized = serde.deserialize(serialized)

        deserialized shouldBe model
    }
})

fun buildProperty(modelId: ModelId, name: String, declaredType: PropertyValueType, initialValue: PropertyValue? = null): Property {
    return Property(
        modelId,
        PropertyName(name),
        PropertyDescription(""),
        declaredType,
        initialValue,
    )
}
