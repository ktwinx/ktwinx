package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import it.wldt.adapter.digital.DigitalAdapter
import it.wldt.core.engine.DigitalTwin

class DigitalAdapterRegistry(factories: List<DigitalAdapterFactory>) {
    private val byType: Map<DigitalInterfaceType, DigitalAdapterFactory> =
        factories.associateBy { it.interfaceType }

    /**
     * Eager validation across all configured interfaces.
     * Aggregates ALL errors before returning — does not short-circuit on first failure.
     */
    fun validateAll(interfaces: List<DigitalInterface>): Result<Unit> {
        val errors = mutableListOf<String>()
        interfaces.forEach { dI ->
            val factory = byType[dI.interfaceType]
            if (factory == null) {
                errors += "interface ${dI.id}: no factory registered for type ${dI.interfaceType}"
            } else {
                factory.validate(dI).onFailure { e ->
                    errors += "interface ${dI.id}: ${e.message}"
                }
            }
        }
        return if (errors.isEmpty()) Result.success(Unit)
        else Result.failure(
            IllegalStateException("config validation failed:\n - " + errors.joinToString("\n - "))
        )
    }

    fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*>? =
        byType[dI.interfaceType]?.create(dI, dt, models)
}
