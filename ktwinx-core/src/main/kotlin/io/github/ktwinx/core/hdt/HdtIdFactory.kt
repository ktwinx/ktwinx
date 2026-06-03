package io.github.ktwinx.core.hdt

import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterface
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceId
import io.github.ktwinx.core.hdt.interfaces.digital.DigitalInterfaceName
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterface
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceId
import io.github.ktwinx.core.hdt.interfaces.physical.PhysicalInterfaceName
import io.github.ktwinx.core.hdt.model.Model
import io.github.ktwinx.core.hdt.model.ModelId
import io.github.ktwinx.core.hdt.model.ModelName
import io.github.ktwinx.core.hdt.model.property.PropertyId
import io.github.ktwinx.core.hdt.model.property.PropertyName
import io.github.ktwinx.core.hdt.storage.StorageId
import io.github.ktwinx.core.hdt.storage.StorageName

/**
 * Single source of truth for HDT ID format and construction.
 *
 * Format:
 *   HdtId               → <hdtId>
 *   ModelId             → <hdtId>:<modelName>
 *   PropertyId          → <hdtId>:<modelName>:<propertyName>
 *   StorageId           → <hdtId>:<storageName>
 *   DigitalInterfaceId  → <hdtId>:<interfaceName>
 *   PhysicalInterfaceId → <hdtId>:<interfaceName>
 *
 * Each name segment must be non-blank and must not contain [SEPARATOR].
 */
object HdtIdFactory {

    const val SEPARATOR = ':'

    fun hdtId(raw: String): HdtId = HdtId(raw)

    fun modelId(hdtId: HdtId, name: ModelName): ModelId =
        ModelId("$hdtId$SEPARATOR$name")

    fun propertyId(modelId: ModelId, name: PropertyName): PropertyId =
        PropertyId("$modelId$SEPARATOR$name")

    fun storageId(hdtId: HdtId, name: StorageName): StorageId =
        StorageId("$hdtId$SEPARATOR$name")

    fun digitalInterfaceId(hdtId: HdtId, name: DigitalInterfaceName): DigitalInterfaceId =
        DigitalInterfaceId("$hdtId$SEPARATOR$name")

    fun physicalInterfaceId(hdtId: HdtId, name: PhysicalInterfaceName): PhysicalInterfaceId =
        PhysicalInterfaceId("$hdtId$SEPARATOR$name")
}

/**
 * Returns a copy of this Model renamed to [newName], cascading the ID change to all its Properties.
 */
fun Model.rename(newName: ModelName): Model {
    val newModelId = HdtIdFactory.modelId(hdtId, newName)
    return copy(
        name = newName,
        properties = properties.map { it.copy(modelId = newModelId) },
    )
}

/**
 * Returns a copy of this HDT with the named model renamed to [newName],
 * cascading the ID change to all that model's Properties.
 */
fun HumanDigitalTwin.renameModel(oldName: ModelName, newName: ModelName): HumanDigitalTwin {
    require(models.any { it.name == oldName }) {
        "No model named '$oldName' in HDT '$hdtId'"
    }
    return copy(
        models = models.map { if (it.name == oldName) it.rename(newName) else it },
    )
}

/**
 * Returns a copy of this HDT with a new [newHdtId], cascading the change to all child entity IDs
 * (Models, Properties, Storages, Physical Interfaces, Digital Interfaces).
 */
fun HumanDigitalTwin.rename(newHdtId: HdtId): HumanDigitalTwin {
    val updatedModels = models.map { model ->
        val newModelId = HdtIdFactory.modelId(newHdtId, model.name)
        model.copy(
            hdtId = newHdtId,
            properties = model.properties.map { it.copy(modelId = newModelId) },
        )
    }
    return copy(
        hdtId = newHdtId,
        models = updatedModels,
        storages = storages.map { it.copy(hdtId = newHdtId) },
        physicalInterfaces = physicalInterfaces.map { it.withHdtId(newHdtId) },
        digitalInterfaces = digitalInterfaces.map { it.withHdtId(newHdtId) },
    )
}

private fun PhysicalInterface.withHdtId(newHdtId: HdtId): PhysicalInterface = copy(hdtId = newHdtId)

private fun DigitalInterface.withHdtId(newHdtId: HdtId): DigitalInterface = copy(hdtId = newHdtId)