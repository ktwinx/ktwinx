package io.github.ktwinx.wldt.plugin.shadowing

import io.github.ktwinx.core.hdt.event.CodingLinkedUpdateEvent
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.property.Coding
import io.github.ktwinx.core.hdt.model.property.Property
import io.github.ktwinx.core.hdt.model.property.toPropertyValue
import io.github.ktwinx.core.hdt.query.propertiesByCoding
import io.github.ktwinx.distributed.serde.Stub
import it.wldt.adapter.digital.event.DigitalActionWldtEvent
import it.wldt.adapter.physical.PhysicalAssetAction
import it.wldt.adapter.physical.PhysicalAssetDescription
import it.wldt.adapter.physical.PhysicalAssetEvent
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent
import it.wldt.core.state.DigitalTwinStateAction
import it.wldt.core.state.DigitalTwinStateEvent
import it.wldt.core.state.DigitalTwinStateEventNotification
import it.wldt.core.state.DigitalTwinStateProperty
import it.wldt.exception.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

class KtwinxShadowingFunction(id: String, val models: List<Model>): it.wldt.core.model.ShadowingFunction(id) {
    val logger: Logger = LoggerFactory.getLogger(KtwinxShadowingFunction::class.java)

    private lateinit var propertyByKey: Map<String, Property>
    private lateinit var codingIndex: Map<Coding, List<Property>>

    override fun onCreate() {
        logger.debug("Shadowing - OnCreate")
        setupStartingModels()
    }

    override fun onStart() {
        logger.debug("Shadowing - OnStart")
    }

    override fun onStop() {
        logger.debug("Shadowing - OnStop")
    }

    override fun onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap: Map<String, PhysicalAssetDescription>) {
        logger.debug("Shadowing - onDtBound")

        try {
            startShadowing(adaptersPhysicalAssetDescriptionMap)

            // Observe all available properties
            this.observePhysicalAssetProperties(
                adaptersPhysicalAssetDescriptionMap
                    .values
                    .flatMap { it.properties }
                    .toList()
            )

            // observes all the available events
            this.observePhysicalAssetEvents(
                adaptersPhysicalAssetDescriptionMap.values
                    .flatMap { it.events }
                    .toList()
            )

            this.observeDigitalActionEvents()
        } catch (e: EventBusException) {
            logger.error("Error during digital twin binding", e)
        } catch (e: ModelException) {
            logger.error("Error during digital twin binding", e)
        } catch (e: WldtDigitalTwinStateException) {
            logger.error("Error during digital twin binding", e)
        }
    }

    override fun onDigitalTwinUnBound(
        adaptersPhysicalAssetDescriptionMap: Map<String?, PhysicalAssetDescription?>?,
        errorMessage: String?
    ) {
        logger.debug("Shadowing - onDtUnBound")
    }

    override fun onPhysicalAdapterBidingUpdate(
        adapterId: String?,
        adapterPhysicalAssetDescription: PhysicalAssetDescription?
    ) {
        logger.info(
            "Shadowing - onPABindingUpdate - updated Adapter: {}, new PAD: {}", adapterId,
            adapterPhysicalAssetDescription
        )
    }

    override fun onPhysicalAssetPropertyVariation(physicalPropertyEventMessage: PhysicalAssetPropertyWldtEvent<*>?) {
        logger.info(
            "Shadowing - onPAPropertyVariation - property event: {} ",
            physicalPropertyEventMessage
        )

        // Update Digital Twin Status
        try {
            this.digitalTwinStateManager.startStateTransaction()

            this.digitalTwinStateManager.updateProperty(
                DigitalTwinStateProperty(
                    physicalPropertyEventMessage!!.physicalPropertyId,
                    physicalPropertyEventMessage.getBody()
                )
            )

            this.digitalTwinStateManager.commitStateTransaction()
        } catch (e: java.lang.Exception) {
            logger.error(e.message, e)
        }

        emitCodingLinkedUpdateIfAny(physicalPropertyEventMessage!!)
    }

    override fun onPhysicalAssetEventNotification(physicalAssetEventWldtEvent: PhysicalAssetEventWldtEvent<*>?) {
        logger.info(
            "Shadowing - onPhysicalAssetEventNotification - received Event:{}",
            physicalAssetEventWldtEvent
        )
        try {
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(
                DigitalTwinStateEventNotification<String?>(
                    physicalAssetEventWldtEvent!!.physicalEventKey,
                    physicalAssetEventWldtEvent.getBody() as String?,
                    System.currentTimeMillis()
                )
            )
        } catch (e: WldtDigitalTwinStateEventNotificationException) {
            logger.error(e.message, e)
        }
    }

    override fun onPhysicalAssetRelationshipEstablished(physicalAssetRelationshipWldtEvent: PhysicalAssetRelationshipInstanceCreatedWldtEvent<*>?) {

    }

    override fun onPhysicalAssetRelationshipDeleted(physicalAssetRelationshipWldtEvent: PhysicalAssetRelationshipInstanceDeletedWldtEvent<*>?) {

    }

    override fun onDigitalActionEvent(digitalActionWldtEvent: DigitalActionWldtEvent<*>?) {
        logger.info("Shadowing - onDigitalActionEvent - received:{}", digitalActionWldtEvent)
        try {
            publishPhysicalAssetActionWldtEvent(
                digitalActionWldtEvent!!.actionKey,
                digitalActionWldtEvent.getBody()
            )
        } catch (e: EventBusException) {
            logger.error(e.message, e)
        }
    }

    private fun setupStartingModels() {
        logger.debug("Setting up models for shadowing")
        val allProps = models.flatMap { it.properties }
        propertyByKey = allProps.associateBy { it.id.toString() }
        codingIndex = allProps.propertiesByCoding()
        logger.debug(
            "Indexed {} properties; {} distinct codings",
            propertyByKey.size, codingIndex.size,
        )
    }

    private fun emitCodingLinkedUpdateIfAny(
        physicalPropertyEventMessage: PhysicalAssetPropertyWldtEvent<*>,
    ) {
        val key = physicalPropertyEventMessage.physicalPropertyId
        val source = propertyByKey[key] ?: return
        val coding = source.coding ?: return
        val siblings = (codingIndex[coding].orEmpty()).filter { it.id != source.id }
        if (siblings.isEmpty()) return

        val newValue = physicalPropertyEventMessage.getBody().toPropertyValue() ?: run {
            logger.warn(
                "Coding-linked update suppressed: unsupported value type {} for property {}",
                physicalPropertyEventMessage.getBody()?.javaClass, source.id,
            )
            return
        }

        val payload = CodingLinkedUpdateEvent(
            sourcePropertyId = source.id,
            coding = coding,
            linkedPropertyIds = siblings.map { it.id },
            newValue = newValue,
        )
        try {
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(
                DigitalTwinStateEventNotification<String?>(
                    CodingLinkedUpdateEvent.WLDT_EVENT_KEY,
                    Stub.codingLinkedUpdateEventSerDe().serialize(payload),
                    System.currentTimeMillis(),
                )
            )
        } catch (e: WldtDigitalTwinStateEventNotificationException) {
            logger.error("Failed to emit coding-linked update event: {}", e.message, e)
        }
    }

    private fun startShadowing(adaptersPhysicalAssetDescriptionMap: Map<String, PhysicalAssetDescription>) {
        this.digitalTwinStateManager.startStateTransaction()

        adaptersPhysicalAssetDescriptionMap.forEach { (id, pad) ->
            pad.properties.forEach { p ->
                try {
                    if (!this.digitalTwinStateManager.digitalTwinState.containsProperty(p.key)) {
                        this.digitalTwinStateManager
                            .createProperty(DigitalTwinStateProperty<Any?>(p.key, p.getInitialValue()))
                    }
                } catch (e: WldtDigitalTwinStateException) {
                    logger.error("Error creating property for PAD: {}", id, e)
                } catch (e: WldtDigitalTwinStatePropertyException) {
                    logger.error("Error creating property for PAD: {}", id, e)
                }
            }
            pad.actions.forEach(Consumer { a: PhysicalAssetAction? ->
                try {
                    if (!this.digitalTwinStateManager.digitalTwinState.containsAction(a!!.key)) {
                        this.digitalTwinStateManager
                            .enableAction(DigitalTwinStateAction(a.key, a.type, a.contentType))
                    }
                } catch (e: WldtDigitalTwinStateException) {
                    logger.error("Error enabling action for PAD: {}", id, e)
                } catch (e: WldtDigitalTwinStateActionException) {
                    logger.error("Error enabling action for PAD: {}", id, e)
                }
            })
            pad.events.forEach(Consumer { e: PhysicalAssetEvent? ->
                try {
                    if (!this.digitalTwinStateManager.digitalTwinState.containsEvent(e!!.key)) {
                        this.digitalTwinStateManager.registerEvent(DigitalTwinStateEvent(e.key, e.type))
                    }
                } catch (ex: WldtDigitalTwinStateException) {
                    logger.error("Error registering event for PAD: {}", id, ex)
                } catch (ex: WldtDigitalTwinStateEventException) {
                    logger.error("Error registering event for PAD: {}", id, ex)
                }
            })

            try {
                if (!this.digitalTwinStateManager.digitalTwinState.containsEvent(CodingLinkedUpdateEvent.WLDT_EVENT_KEY)) {
                    this.digitalTwinStateManager.registerEvent(
                        DigitalTwinStateEvent(
                            CodingLinkedUpdateEvent.WLDT_EVENT_KEY,
                            "coding-linked-update",
                        )
                    )
                }
            } catch (e: WldtDigitalTwinStateException) {
                logger.error("Error registering coding-linked-update event: {}", id, e)
            } catch (e: WldtDigitalTwinStateEventException) {
                logger.error("Error registering coding-linked-update event: {}", id, e)
            }

            this.digitalTwinStateManager.commitStateTransaction()

            notifyShadowingSync()
        }
    }
}
