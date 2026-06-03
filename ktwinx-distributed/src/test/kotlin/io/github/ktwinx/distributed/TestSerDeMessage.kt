package io.github.ktwinx.distributed

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.PropertyDescription
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.model.property.PropertyValue.Companion.pv
import io.github.ktwinx.core.hdt.model.property.PropertyValueType
import io.github.ktwinx.distributed.id.SenderId
import io.github.ktwinx.distributed.message.Message
import io.github.ktwinx.distributed.serde.Stub
import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals
import kotlin.time.Clock

class TestSerDeMessage: FunSpec({
    fun Message.assertEqualsIgnoringReceivedAt(other: Message) {
        assertEquals(hdt, other.hdt)
        assertEquals(sender, other.sender)
        assertEquals(sentAt, other.sentAt)
        assertEquals(payload, other.payload)
    }


    test("Serialize Message") {
      val hdtId = HdtId("1")
      val now = Clock.System.now()
      val property = Property(
          ModelId("my-model"),
          PropertyName("my-property"),
          PropertyDescription(""),
          PropertyValueType.STRING,
          "test-property".pv(),
      )
      val message = Message(
          hdt = hdtId,
          sender = SenderId("test-engine"),
          sentAt = now.toEpochMilliseconds(),
          receivedAt = now.toEpochMilliseconds(),
          payload = Stub.propertyJsonSerDe().serializeToJsonElement(property)
      )
      val serialized = Stub.messageJsonSerDe().serialize(message)
      //println(serialized)
      val deserialized = Stub.messageJsonSerDe().deserialize(serialized)
      message.assertEqualsIgnoringReceivedAt(deserialized)
      val deserializedProperty = Stub.propertyJsonSerDe().deserializeFromJsonElement(deserialized.payload)
      assertEquals(property, deserializedProperty)
  }
})