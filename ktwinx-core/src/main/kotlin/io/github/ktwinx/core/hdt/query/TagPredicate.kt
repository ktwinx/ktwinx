package io.github.ktwinx.core.hdt.query

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Boolean algebra over a `Map<String, String>` of tags. Reified as data so it
 * can be serialized, persisted, and later compiled to a backend query
 * (a named View stores a [TagPredicate] verbatim).
 *
 * Operates exclusively on [io.github.whdt.core.hdt.model.property.Property.tags];
 * does not see `coding`, `format`, or any other Property field.
 *
 * Identity laws:
 *  - `And(emptyList())` evaluates to `true`  (vacuous conjunction).
 *  - `Or(emptyList())`  evaluates to `false` (vacuous disjunction).
 */
@Serializable
sealed interface TagPredicate {
    @Serializable @SerialName("eq")
    data class Eq(val key: String, val value: String) : TagPredicate

    @Serializable @SerialName("in")
    data class In(val key: String, val values: Set<String>) : TagPredicate

    @Serializable @SerialName("has")
    data class Has(val key: String) : TagPredicate

    @Serializable @SerialName("and")
    data class And(val terms: List<TagPredicate>) : TagPredicate

    @Serializable @SerialName("or")
    data class Or(val terms: List<TagPredicate>) : TagPredicate

    @Serializable @SerialName("not")
    data class Not(val term: TagPredicate) : TagPredicate
}

fun TagPredicate.matches(tags: Map<String, String>): Boolean = when (this) {
    is TagPredicate.Eq  -> tags[key] == value
    is TagPredicate.In  -> tags[key] in values
    is TagPredicate.Has -> key in tags
    is TagPredicate.And -> terms.all { it.matches(tags) }
    is TagPredicate.Or  -> terms.any { it.matches(tags) }
    is TagPredicate.Not -> !term.matches(tags)
}
