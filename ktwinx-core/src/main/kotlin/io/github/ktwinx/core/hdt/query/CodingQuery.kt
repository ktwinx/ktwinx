package io.github.ktwinx.core.hdt.query

import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property

/**
 * Returns every Property whose [Property.coding] equals [coding]. Properties
 * with `coding == null` are skipped. Result preserves input order.
 */
fun List<Property>.findByCoding(coding: Coding): List<Property> =
    filter { it.coding == coding }

/**
 * Indexes Properties by their non-null [Property.coding]. Properties with
 * `coding == null` are excluded entirely (they are not linkable). Two
 * Properties sharing a Coding will be grouped together; this is the basis
 * on which the shadowing function detects cross-Model linkage.
 */
fun List<Property>.propertiesByCoding(): Map<Coding, List<Property>> =
    filter { it.coding != null }.groupBy { it.coding!! }
