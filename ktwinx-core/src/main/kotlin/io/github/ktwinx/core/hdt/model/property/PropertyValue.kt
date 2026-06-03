package io.github.ktwinx.core.hdt.model.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the value of a property in the system.
 */
@Serializable
sealed class PropertyValue {
    @Serializable
    @SerialName("empty-value")
    object EmptyPropertyValue: PropertyValue()
    @Serializable
    @SerialName("string-value")
    data class StringPropertyValue(val value: String) : PropertyValue()
    @Serializable
    @SerialName("int-value")
    data class IntPropertyValue(val value: Int) : PropertyValue()
    @Serializable
    @SerialName("float-value")
    data class FloatPropertyValue(val value: Float) : PropertyValue()
    @Serializable
    @SerialName("boolean-value")
    data class BooleanPropertyValue(val value: Boolean) : PropertyValue()
    @Serializable
    @SerialName("double-value")
    data class DoublePropertyValue(val value: Double) : PropertyValue()
    @Serializable
    @SerialName("long-value")
    data class LongPropertyValue(val value: Long) : PropertyValue()

    companion object {
        fun Int.pv(): PropertyValue = IntPropertyValue(this)
        fun Float.pv(): PropertyValue = FloatPropertyValue(this)
        fun Boolean.pv(): PropertyValue = BooleanPropertyValue(this)
        fun Double.pv(): PropertyValue = DoublePropertyValue(this)
        fun Long.pv(): PropertyValue = LongPropertyValue(this)
        fun String.pv(): PropertyValue = StringPropertyValue(this)
    }
}

/**
 * Maps a JVM value into a [PropertyValue].
 * - `null` → [PropertyValue.EmptyPropertyValue]
 * - `String`, `Int`, `Long`, `Float`, `Double`, `Boolean` → matching wrapper
 * - anything else → `null`
 *
 * Top-level (not in [PropertyValue.Companion]) to avoid overload ambiguity
 * with the primitive `.pv()` helpers and to signal that this conversion may fail.
 */
fun Any?.toPropertyValue(): PropertyValue? = when (this) {
    null       -> PropertyValue.EmptyPropertyValue
    is String  -> PropertyValue.StringPropertyValue(this)
    is Int     -> PropertyValue.IntPropertyValue(this)
    is Long    -> PropertyValue.LongPropertyValue(this)
    is Float   -> PropertyValue.FloatPropertyValue(this)
    is Double  -> PropertyValue.DoublePropertyValue(this)
    is Boolean -> PropertyValue.BooleanPropertyValue(this)
    else       -> null
}