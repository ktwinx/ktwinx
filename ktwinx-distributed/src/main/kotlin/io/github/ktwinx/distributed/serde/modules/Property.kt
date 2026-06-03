package io.github.ktwinx.distributed.serde.modules

import io.github.ktwinx.core.hdt.model.property.PropertyValue
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val propertyModule = SerializersModule {
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