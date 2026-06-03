package io.github.ktwinx.core.hdt.query

import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property

/**
 * Returns the properties whose [Property.tags] satisfy [predicate].
 * Preserves the input list order.
 */
fun List<Property>.filterByTags(predicate: TagPredicate): List<Property> =
    filter { predicate.matches(it.tags) }

/**
 * Groups by the value of tag [key]. Properties missing the key are placed under
 * the `null` bucket. Result is a [LinkedHashMap]; group keys appear in the order
 * they were first encountered while scanning the input.
 */
fun List<Property>.groupByTag(key: String): Map<String?, List<Property>> {
    val result = LinkedHashMap<String?, MutableList<Property>>()
    for (p in this) {
        val v: String? = p.tags[key]
        result.getOrPut(v) { mutableListOf() } += p
    }
    return result
}

/**
 * Hierarchical grouping. Applies [groupByTag] to each bucket of an existing
 * group result. Both levels preserve insertion order; both can have a `null`
 * bucket independently.
 */
fun Map<String?, List<Property>>.thenGroupByTag(key: String): Map<String?, Map<String?, List<Property>>> =
    mapValues { (_, props) -> props.groupByTag(key) }

/**
 * Flat view of every Property declared across every Model of [this] HDT,
 * preserving Model order and intra-Model property order. Each Property
 * retains its `modelId`, so consumers can recover provenance after filtering.
 */
fun HumanDigitalTwin.allProperties(): List<Property> =
    models.flatMap { it.properties }

fun HumanDigitalTwin.findByCoding(coding: Coding): List<Property> =
    allProperties().findByCoding(coding)

fun HumanDigitalTwin.propertiesByCoding(): Map<Coding, List<Property>> =
    allProperties().propertiesByCoding()
