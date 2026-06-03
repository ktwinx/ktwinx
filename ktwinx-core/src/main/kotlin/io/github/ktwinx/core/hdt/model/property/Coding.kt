package io.github.ktwinx.core.hdt.model.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * FHIR-style concept identity for a Property. Two Properties in different Models
 * that share the same Coding represent the same physical concept (e.g. systolic
 * blood pressure observed in a raw Model vs. a FHIR-shaped Model).
 *
 * - [system]: the vocabulary the code belongs to ("loinc", "snomed", or a
 *   project-local namespace like "preterm-2024" for custom concepts).
 * - [code]: the identifier within that vocabulary ("8480-6", "SENv1", …).
 *
 * Cross-Model linking is opt-in. Properties without a Coding live in a single
 * Model and are not auto-propagated by downstream consumers; this is intentional.
 */
@Serializable
@SerialName("coding")
data class Coding(
    val system: String,
    val code: String,
) {
    init {
        require(system.isNotBlank()) { "Coding.system must not be blank" }
        require(code.isNotBlank())   { "Coding.code must not be blank" }
    }
}
