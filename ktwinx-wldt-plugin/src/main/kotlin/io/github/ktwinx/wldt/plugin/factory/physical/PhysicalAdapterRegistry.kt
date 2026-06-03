package io.github.ktwinx.wldt.plugin.factory.physical

import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import it.wldt.adapter.physical.PhysicalAdapter

class PhysicalAdapterRegistry(factories: List<PhysicalAdapterFactory>) {
    private val byType: Map<PhysicalInterfaceType, PhysicalAdapterFactory> =
        factories.associateBy { it.interfaceType }

    /**
     * Eager validation across all configured interfaces.
     * Aggregates ALL errors before returning — does not short-circuit on first failure.
     */
    fun validateAll(interfaces: List<PhysicalInterface>): Result<Unit> {
        val errors = mutableListOf<String>()
        interfaces.forEach { pI ->
            val factory = byType[pI.interfaceType]
            if (factory == null) {
                errors += "interface ${pI.id}: no factory registered for type ${pI.interfaceType}"
            } else {
                factory.validate(pI).onFailure { e ->
                    errors += "interface ${pI.id}: ${e.message}"
                }
            }
        }
        return if (errors.isEmpty()) Result.success(Unit)
        else Result.failure(
            IllegalStateException("config validation failed:\n - " + errors.joinToString("\n - "))
        )
    }

    fun create(pI: PhysicalInterface, models: List<Model>): PhysicalAdapter? =
        byType[pI.interfaceType]?.create(pI, models)
}
