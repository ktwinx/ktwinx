package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.query.TagPredicate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Identifier of a [View]. Globally identifying within whatever scope stores Views. */
@JvmInline @Serializable
value class ViewName(val value: String) {
    init { require(value.isNotBlank()) { "ViewName must not be blank" } }
    override fun toString(): String = value
}

/**
 * A named, persistable query spec.
 *
 * Views are HDT-agnostic data: the same View runs against any [List]<[io.github.whdt.core.hdt.model.property.Property]>,
 * a single [io.github.whdt.core.hdt.model.Model], a single [io.github.whdt.core.hdt.HumanDigitalTwin],
 * or a [List]<[io.github.whdt.core.hdt.HumanDigitalTwin]> population. See `View.execute(...)` overloads.
 *
 * Execution semantics:
 *  - If [predicate] is non-null, properties are filtered by it.
 *  - If [groupByKeys] is empty, the result is [ViewResult.Flat]; otherwise it is a nested [ViewResult.Grouped]
 *    one level deep per key in order. The null bucket at each level holds properties missing that tag key.
 */
@Serializable
@SerialName("view")
data class View(
    val name: ViewName,
    val predicate: TagPredicate? = null,
    val groupByKeys: List<String> = emptyList(),
) {
    init {
        require(groupByKeys.all { it.isNotBlank() }) {
            "View '$name': groupByKeys must not contain blank entries: $groupByKeys"
        }
    }
}
