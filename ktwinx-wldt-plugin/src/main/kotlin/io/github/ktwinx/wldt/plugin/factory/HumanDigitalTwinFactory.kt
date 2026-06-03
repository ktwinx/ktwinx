package io.github.ktwinx.wldt.plugin.factory

import io.github.ktwinx.core.hdt.HumanDigitalTwin
import io.github.ktwinx.core.hdt.storage.StorageType
import io.github.ktwinx.distributed.serde.Stub
import io.github.ktwinx.wldt.plugin.factory.digital.DigitalAdapterRegistry
import io.github.ktwinx.wldt.plugin.factory.digital.HttpDigitalAdapterFactory
import io.github.ktwinx.wldt.plugin.factory.digital.MqttDigitalAdapterFactory
import io.github.ktwinx.wldt.plugin.factory.physical.MqttPhysicalAdapterFactory
import io.github.ktwinx.wldt.plugin.factory.physical.PhysicalAdapterRegistry
import io.github.ktwinx.wldt.plugin.shadowing.KtwinxShadowingFunction
import it.wldt.core.engine.DigitalTwin
import it.wldt.storage.DefaultWldtStorage
import java.util.logging.Logger

object HumanDigitalTwinFactory {
    val logger: Logger = Logger.getLogger("HumanDigitalTwinFactory")
    val observationSerDe = Stub.observationJsonSerDe()

    private val digitalRegistry =
        DigitalAdapterRegistry(
            listOf(
                MqttDigitalAdapterFactory(
                    observationSerDe
                ),
                HttpDigitalAdapterFactory(),
            )
        )

    private val physicalRegistry =
        PhysicalAdapterRegistry(
            listOf(
                MqttPhysicalAdapterFactory(
                    observationSerDe
                ),
            )
        )

    fun fromHumanDigitalTwin(hdt: HumanDigitalTwin): DigitalTwin {
        val shad = KtwinxShadowingFunction(
            "${hdt.hdtId}-shadowing-function",
            hdt.models
        )
        val dt = DigitalTwin(hdt.hdtId.id, shad)

        physicalRegistry.validateAll(hdt.physicalInterfaces).getOrThrow()
        hdt.physicalInterfaces.forEach { pI ->
            physicalRegistry.create(pI, hdt.models)?.let { dt.addPhysicalAdapter(it) }
                ?: logger.warning("no factory registered for interface type ${pI.interfaceType}")
        }

        digitalRegistry.validateAll(hdt.digitalInterfaces).getOrThrow()
        hdt.digitalInterfaces.forEach { dI ->
            digitalRegistry.create(dI, dt, hdt.models)?.let { dt.addDigitalAdapter(it) }
                ?: logger.warning("no factory registered for interface type ${dI.interfaceType}")
        }

        val storages = hdt.storages.map { storage ->
            when (storage.storageType) {
                StorageType.IN_MEMORY -> DefaultWldtStorage("${hdt.hdtId}-default-storage", true)
                else -> DefaultWldtStorage("${hdt.hdtId}-default-storage", true)
            }
        }

        storages.forEach { dt.storageManager.putStorage(it) }

        return dt
    }
}
