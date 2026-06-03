package io.github.ktwinx.wldt.plugin.factory.physical

import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.property.PropertyObservation
import io.github.ktwinx.distributed.namespace.Namespace
import io.github.ktwinx.distributed.serde.SerDe
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration

class MqttPhysicalAdapterFactory(
    private val observationSerDe: SerDe<PropertyObservation>,
) : io.github.ktwinx.wldt.plugin.factory.physical.PhysicalAdapterFactory {
    override val interfaceType = PhysicalInterfaceType.MQTT

    override fun validate(pI: PhysicalInterface): Result<Unit> = runCatching {
        pI.optionalString("broker",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.physical.MqttPhysicalAdapterFactory.Companion.DEFAULT_BROKER
        )
        pI.optionalInt("port",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.physical.MqttPhysicalAdapterFactory.Companion.DEFAULT_PORT
        )
    }

    override fun create(pI: PhysicalInterface, models: List<Model>): MqttPhysicalAdapter {
        val broker = pI.optionalString("broker",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.physical.MqttPhysicalAdapterFactory.Companion.DEFAULT_BROKER
        )
        val port = pI.optionalInt("port",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.physical.MqttPhysicalAdapterFactory.Companion.DEFAULT_PORT
        )
        val builder = MqttPhysicalAdapterConfiguration.builder(broker, port)
        models.flatMap { it.properties }.forEach { property ->
            builder.addPhysicalAssetPropertyAndTopic(
                property.id.toString(),
                property.initialValue ?: property.declaredType.defaultFor(),
                Namespace.propertyUpdateRequestTopic(pI.hdtId, property.name)
            ) { string ->
                observationSerDe.deserialize(string).value
            }
        }
        return MqttPhysicalAdapter(pI.id.toString(), builder.build())
    }

    private companion object {
        const val DEFAULT_BROKER = "localhost"
        const val DEFAULT_PORT = 1883
    }
}
