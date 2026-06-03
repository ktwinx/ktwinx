package io.github.ktwinx.core.hdt.storage

import io.github.ktwinx.core.hdt.HdtId
import io.github.ktwinx.core.hdt.HdtIdFactory
import kotlinx.serialization.Serializable

@Serializable
enum class StorageType {
    IN_MEMORY,
    DB_MONGO,
    DB_POSTGRESQL,
}

@JvmInline @Serializable value class StorageName(val value: String) {
    init {
        require(value.isNotBlank()) { "StorageName must not be blank" }
        require(':' !in value) { "StorageName must not contain ':'" }
    }

    override fun toString(): String = value
}

@JvmInline @Serializable value class StorageId(val value: String) {
    override fun toString(): String = value
}

@Serializable
data class Storage(
    val hdtId: HdtId,
    val name: StorageName,
    val storageType: StorageType,
    val config: Map<String, String> = emptyMap(),
) {
    val id = HdtIdFactory.storageId(hdtId, name)

    fun addConfig(c: Map<String, String>): Storage {
        return copy(config = config + c)
    }

    fun addConfigProperty(p: Pair<String, String>): Storage {
        return copy(config = config.plus(p))
    }

    fun removeConfigProperty(p: String): Storage {
        return copy(config = config.minus(p))
    }

    companion object {
        fun default(hdtId: HdtId): Storage {
            return Storage(
                hdtId = hdtId,
                name = StorageName("memory-storage"),
                storageType = StorageType.IN_MEMORY,
            )
        }
    }
}