package io.github.ktwinx.core.hdt

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.storage.Storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline @Serializable value class HdtId(val id: String) {
    init {
        require(id.isNotBlank()) { "HdtId must not be blank" }
        require(':' !in id) { "HdtId must not contain ':'" }
    }

    override fun toString(): String = id
}

/**
 * Represents a Human Digital Twin (HDT), a digital representation of a human being.
 */
@Serializable
@SerialName("human-digital-twin")
data class HumanDigitalTwin(
    val hdtId: HdtId,
    val models: List<Model> = emptyList(),
    val physicalInterfaces: List<PhysicalInterface> = emptyList(),
    val digitalInterfaces: List<DigitalInterface> = emptyList(),
    val storages: List<Storage> = listOf(Storage.default(hdtId)),
    val tags: Map<String, String> = emptyMap(),
) {
    init {
        val dupModels = models.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(dupModels.isEmpty()) { "Duplicate model IDs in HDT '$hdtId': $dupModels" }
        require(models.all { it.hdtId == hdtId }) {
            "All models must reference HDT '$hdtId'; mismatched: ${models.filter { it.hdtId != hdtId }.map { it.id }}"
        }

        val dupStorages = storages.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(dupStorages.isEmpty()) { "Duplicate storage IDs in HDT '$hdtId': $dupStorages" }
        require(storages.all { it.hdtId == hdtId }) {
            "All storages must reference HDT '$hdtId'; mismatched: ${storages.filter { it.hdtId != hdtId }.map { it.id }}"
        }

        val dupPhysical = physicalInterfaces.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(dupPhysical.isEmpty()) { "Duplicate physical interface IDs in HDT '$hdtId': $dupPhysical" }
        require(physicalInterfaces.all { it.hdtId == hdtId }) {
            "All physical interfaces must reference HDT '$hdtId'; mismatched: ${physicalInterfaces.filter { it.hdtId != hdtId }.map { it.id }}"
        }

        val dupDigital = digitalInterfaces.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(dupDigital.isEmpty()) { "Duplicate digital interface IDs in HDT '$hdtId': $dupDigital" }
        require(digitalInterfaces.all { it.hdtId == hdtId }) {
            "All digital interfaces must reference HDT '$hdtId'; mismatched: ${digitalInterfaces.filter { it.hdtId != hdtId }.map { it.id }}"
        }
    }
}
