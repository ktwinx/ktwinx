package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.storage.Storage
import io.github.ktwinx.core.hdt.storage.StorageName
import io.github.ktwinx.core.hdt.storage.StorageType
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

class TestSerDeHdt: FunSpec({
  test("Test SerDe HumanDigitalTwin") {
      val hdtId = HdtId("hdt-1")
      val modelName = ModelName("my-model")
      val modelId = ModelId("$hdtId:$modelName")
      val properties = listOf(
          testProperty(modelId, PropertyName("First Name"), PropertyValueType.STRING, "John".pv()),
          testProperty(modelId, PropertyName("Surname"), PropertyValueType.STRING, "Doe".pv())
      )
      val model = Model(hdtId, modelName, ModelDescription("Test Model"), properties)
      val pI = PhysicalInterface(
          interfaceType = PhysicalInterfaceType.MQTT,
          hdtId = hdtId,
          name = PhysicalInterfaceName("mqtt-phys-int")
      )
      val dI = DigitalInterface(
          interfaceType = DigitalInterfaceType.MQTT,
          hdtId = hdtId,
          name = DigitalInterfaceName("mqtt-digital-int")
      )
      val storage = Storage(
          hdtId = hdtId,
          name = StorageName("memory-storage"),
          storageType = StorageType.IN_MEMORY,
          config = mapOf("host" to "localhost", "port" to "27017"),
      )
      val hdt = HumanDigitalTwin(
          hdtId = hdtId,
          models = listOf(model),
          physicalInterfaces = listOf(pI),
          digitalInterfaces = listOf(dI),
          storages = listOf(storage),
      )

      val serialized = Stub.hdtJsonSerDe().serialize(hdt)
      println(serialized)
      val deserialized = Stub.hdtJsonSerDe().deserialize(serialized)

      deserialized shouldBe hdt
  }
}) {
    companion object {
        fun testProperty(modelId: ModelId, name: PropertyName, declaredType: PropertyValueType, initialValue: PropertyValue? = null): Property {
            return Property(
                modelId = modelId,
                name = name,
                description = PropertyDescription(""),
                declaredType = declaredType,
                initialValue = initialValue,
            )
        }
    }
}
