package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import it.wldt.adapter.digital.DigitalAdapter
import it.wldt.core.engine.DigitalTwin

interface DigitalAdapterFactory {
    val interfaceType: DigitalInterfaceType

    /**
     * Returns Result.success(Unit) if the config is well-formed for this factory.
     * Returns Result.failure(ConfigException) if any required key is missing or any present key is malformed.
     * Validation must not have side effects.
     */
    fun validate(dI: DigitalInterface): Result<Unit>

    /**
     * Constructs the adapter. Caller must validate first; create() may throw on bad config.
     */
    fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): DigitalAdapter<*>
}
