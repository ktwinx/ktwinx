package io.github.ktwinx.distributed.namespace

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.model.property.PropertyName

/**
 * This singleton is used to get correct topics and names for distributed WHDT applications and services.
 */
object Namespace {
    const val MQTT_PREFIX = "whdt"
    const val PROPERTY_UPDATE_NOTIFICATION_MQTT = "property-update-notification"
    const val PROPERTY_UPDATE_REQUEST_MQTT = "property-update-request"

    /**
     * @param hdtId identifier for the hdt
     * @return the MQTT topic of a property update request for a hdt with the specified [HdtId].
     */
    fun propertyUpdateRequestTopic(hdtId: HdtId, propertyName: PropertyName) = "$MQTT_PREFIX/$hdtId/$PROPERTY_UPDATE_REQUEST_MQTT/$propertyName"

    /**
     * @param hdtId identifier for the hdt
     * @return the MQTT topic of a property update notification for a hdt with the specified [HdtId].
     */
    fun propertyUpdateNotificationTopic(hdtId: HdtId, propertyName: PropertyName) = "$MQTT_PREFIX/$hdtId/$PROPERTY_UPDATE_NOTIFICATION_MQTT/$propertyName"
}