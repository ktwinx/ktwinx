package io.github.ktwinx.core.hdt.model.property

import kotlinx.serialization.Serializable

@Serializable
enum class PropertyValueType {
    EMPTY, STRING, INT, LONG, FLOAT, DOUBLE, BOOLEAN;

    fun defaultFor(): PropertyValue = when (this) {
        EMPTY   -> PropertyValue.EmptyPropertyValue
        STRING  -> PropertyValue.StringPropertyValue("")
        INT     -> PropertyValue.IntPropertyValue(0)
        LONG    -> PropertyValue.LongPropertyValue(0L)
        FLOAT   -> PropertyValue.FloatPropertyValue(0f)
        DOUBLE  -> PropertyValue.DoublePropertyValue(0.0)
        BOOLEAN -> PropertyValue.BooleanPropertyValue(false)
    }
}

fun PropertyValue.valueType(): PropertyValueType = when (this) {
    is PropertyValue.EmptyPropertyValue   -> PropertyValueType.EMPTY
    is PropertyValue.StringPropertyValue  -> PropertyValueType.STRING
    is PropertyValue.IntPropertyValue     -> PropertyValueType.INT
    is PropertyValue.LongPropertyValue    -> PropertyValueType.LONG
    is PropertyValue.FloatPropertyValue   -> PropertyValueType.FLOAT
    is PropertyValue.DoublePropertyValue  -> PropertyValueType.DOUBLE
    is PropertyValue.BooleanPropertyValue -> PropertyValueType.BOOLEAN
}
