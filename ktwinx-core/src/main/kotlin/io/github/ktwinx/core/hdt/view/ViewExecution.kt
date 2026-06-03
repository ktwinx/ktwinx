package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.query.allProperties
import io.github.ktwinx.core.hdt.query.filterByTags
import io.github.ktwinx.core.hdt.query.groupByTag

/** Execute against an arbitrary property list. The base case all other overloads delegate to. */
fun View.execute(properties: List<Property>): ViewResult {
    val filtered = predicate?.let { properties.filterByTags(it) } ?: properties
    return buildResult(filtered, groupByKeys)
}

private fun buildResult(properties: List<Property>, keys: List<String>): ViewResult {
    if (keys.isEmpty()) return ViewResult.Flat(properties)
    val head = keys.first()
    val tail = keys.drop(1)
    val grouped = properties.groupByTag(head)
    return ViewResult.Grouped(
        key = head,
        buckets = grouped.mapValues { (_, props) -> buildResult(props, tail) },
    )
}

/** Convenience: execute against a single Model. */
fun View.execute(model: Model): ViewResult = execute(model.properties)

/** Convenience: execute against a single HDT (all Models flattened). */
fun View.execute(hdt: HumanDigitalTwin): ViewResult = execute(hdt.allProperties())

/**
 * Execute against an HDT population. Result is keyed by [HdtId] in the order HDTs appear in [population].
 * Each entry is the View result for that HDT independently — no cross-HDT mixing.
 */
fun View.execute(population: List<HumanDigitalTwin>): Map<HdtId, ViewResult> =
    population.associate { it.hdtId to execute(it) }
