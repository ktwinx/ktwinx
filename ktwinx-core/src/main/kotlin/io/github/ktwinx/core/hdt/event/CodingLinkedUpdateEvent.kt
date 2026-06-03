package io.github.ktwinx.core.hdt.event

import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.PropertyId
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Emitted by the shadowing function when a Property carrying a [Coding]
 * has a value variation AND at least one sibling Property in another Model
 * shares the same [Coding]. The event surfaces the linkage; consumers decide
 * what (if anything) to do with sibling properties.
 *
 * The framework does NOT auto-write [newValue] into the linked siblings.
 *
 * Wire format: serialized as JSON via kotlinx.serialization, passed as the
 * String body of a [it.wldt.core.state.DigitalTwinStateEventNotification].
 */
@Serializable
@SerialName("coding-linked-update")
data class CodingLinkedUpdateEvent(
    /** The Property whose value just changed. */
    val sourcePropertyId: PropertyId,
    /** The Coding the source Property carries. */
    val coding: Coding,
    /** Siblings sharing [coding] in OTHER Properties; excludes [sourcePropertyId]. Never empty (the event is suppressed otherwise). */
    val linkedPropertyIds: List<PropertyId>,
    /** The new value of the source Property. */
    val newValue: PropertyValue,
) {
    companion object {
        /** The WLDT event key registered by `WhdtShadowingFunction` and used by subscribers. */
        const val WLDT_EVENT_KEY: String = "whdt.coding-linked-update"
    }
}
