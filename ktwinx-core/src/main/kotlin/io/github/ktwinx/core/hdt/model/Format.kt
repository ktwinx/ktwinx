package io.github.ktwinx.core.hdt.model

import kotlinx.serialization.Serializable

/**
 * Representational format of a Model — what shape does its property pool follow?
 * Free-form by design so projects can declare their own formats; the framework
 * does not dispatch on format yet, but downstream exporters (e.g. FHIR) will.
 * Use [WellKnownFormats] for stable, framework-recognized values.
 */
@JvmInline @Serializable value class Format(val value: String) {
    init { require(value.isNotBlank()) { "Format must not be blank" } }
    override fun toString(): String = value
}

object WellKnownFormats {
    val UNSPECIFIED = Format("unspecified")  // default; explicit "we don't know yet"
    val RAW         = Format("raw")          // bespoke project-internal shape
    val CUSTOM      = Format("custom")       // bespoke, named via tags/description
    val FHIR_R4     = Format("fhir-r4")
}
