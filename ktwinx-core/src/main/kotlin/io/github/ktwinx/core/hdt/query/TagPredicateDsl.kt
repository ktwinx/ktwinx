package io.github.ktwinx.core.hdt.query

/** Builder DSL for [TagPredicate]. Prefix functions only in this iteration. */

fun eq(key: String, value: String): TagPredicate = TagPredicate.Eq(key, value)

fun inSet(key: String, values: Set<String>): TagPredicate = TagPredicate.In(key, values)
fun inSet(key: String, vararg values: String): TagPredicate = TagPredicate.In(key, values.toSet())

fun has(key: String): TagPredicate = TagPredicate.Has(key)

fun and(vararg terms: TagPredicate): TagPredicate = TagPredicate.And(terms.toList())
fun and(terms: List<TagPredicate>): TagPredicate  = TagPredicate.And(terms)

fun or(vararg terms: TagPredicate): TagPredicate = TagPredicate.Or(terms.toList())
fun or(terms: List<TagPredicate>): TagPredicate  = TagPredicate.Or(terms)

fun not(term: TagPredicate): TagPredicate = TagPredicate.Not(term)
