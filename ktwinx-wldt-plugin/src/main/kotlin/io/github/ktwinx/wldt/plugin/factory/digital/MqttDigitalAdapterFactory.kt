package io.github.ktwinx.wldt.plugin.factory.digital

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceType
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.property.PropertyObservation
import io.github.ktwinx.core.hdt.model.property.PropertyValue
import io.github.ktwinx.distributed.namespace.Namespace
import io.github.ktwinx.distributed.serde.SerDe
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel
import it.wldt.core.engine.DigitalTwin
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class MqttDigitalAdapterFactory(
    private val observationSerDe: SerDe<PropertyObservation>,
) : io.github.ktwinx.wldt.plugin.factory.digital.DigitalAdapterFactory {
    override val interfaceType = DigitalInterfaceType.MQTT

    override fun validate(dI: DigitalInterface): Result<Unit> = runCatching {
        dI.optionalString("broker",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.digital.MqttDigitalAdapterFactory.Companion.DEFAULT_BROKER
        )
        dI.optionalInt("port",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.digital.MqttDigitalAdapterFactory.Companion.DEFAULT_PORT
        )
    }

    @OptIn(ExperimentalTime::class)
    override fun create(dI: DigitalInterface, dt: DigitalTwin, models: List<Model>): MqttDigitalAdapter {
        val broker = dI.optionalString("broker",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.digital.MqttDigitalAdapterFactory.Companion.DEFAULT_BROKER
        )
        val port = dI.optionalInt("port",
            _root_ide_package_.io.github.ktwinx.wldt.plugin.factory.digital.MqttDigitalAdapterFactory.Companion.DEFAULT_PORT
        )
        val builder = MqttDigitalAdapterConfiguration.builder(broker, port)
        val hdtId = dI.hdtId
        models.forEach { model ->
            model.properties.forEach { property ->
                val capturedModel = model
                val capturedProperty = property
                builder.addPropertyTopic(
                    property.id.toString(),
                    Namespace.propertyUpdateNotificationTopic(hdtId, property.name),
                    MqttQosLevel.MQTT_QOS_0
                ) { value: Any? ->
                    observationSerDe.serialize(
                        PropertyObservation(
                            hdtId = hdtId,
                            modelId = capturedModel.id,
                            modelName = capturedModel.name,
                            propertyId = capturedProperty.id,
                            propertyName = capturedProperty.name,
                            timestamp = Clock.System.now(),
                            value = value as PropertyValue,
                        )
                    )
                }
            }
        }
        return MqttDigitalAdapter(dI.id.toString(), builder.build())
    }

    private companion object {
        const val DEFAULT_BROKER = "localhost"
        const val DEFAULT_PORT = 1883
    }
}
