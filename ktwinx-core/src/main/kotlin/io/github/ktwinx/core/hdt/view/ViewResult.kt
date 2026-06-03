package io.github.ktwinx.core.hdt.view

import io.github.ktwinx.core.hdt.model.property.Property
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Recursive shape of a [View] execution output.
 *  - [Flat]: leaf — a list of properties matching the View's predicate (or all, if no predicate)
 *    after grouping has terminated.
 *  - [Grouped]: a single level of grouping by tag [key]; each bucket value is itself a [ViewResult],
 *    enabling arbitrary depth.
 *
 * Bucket maps preserve insertion order (LinkedHashMap) per Issue #2's [io.github.ktwinx.core.hdt.query.groupByTag] contract.
 * The null map key represents properties missing the corresponding tag at that level.
 *
 * Serialization note: [Grouped.buckets] uses [BucketListSerializer] to serialize as a JSON array of
 * {key, result} objects rather than a JSON object map. This avoids JSON's inability to represent null
 * keys unambiguously in object notation, and preserves insertion order across the round-trip.
 */
@Serializable
sealed interface ViewResult {

    @Serializable
    @SerialName("flat")
    data class Flat(val properties: List<Property>) : ViewResult

    @Serializable
    @SerialName("grouped")
    data class Grouped(
        val key: String,
        @Serializable(with = BucketListSerializer::class)
        val buckets: Map<String?, ViewResult>,
    ) : ViewResult
}

/** Internal representation used by [BucketListSerializer] to serialize a single bucket entry. */
@Serializable
internal data class Bucket(val key: String?, val result: ViewResult)

/**
 * Serializes [Map]<[String]?, [ViewResult]> as a JSON array of [Bucket] objects.
 * This sidesteps JSON's restriction that object keys must be non-null strings, and
 * preserves the [LinkedHashMap] insertion order across encode→decode round-trips.
 */
internal object BucketListSerializer : KSerializer<Map<String?, ViewResult>> {
    private val delegate by lazy { ListSerializer(Bucket.serializer()) }
    override val descriptor: SerialDescriptor get() = delegate.descriptor
    override fun serialize(encoder: Encoder, value: Map<String?, ViewResult>) =
        delegate.serialize(encoder, value.entries.map { (k, v) -> Bucket(k, v) })
    override fun deserialize(decoder: Decoder): Map<String?, ViewResult> =
        delegate.deserialize(decoder).associateTo(LinkedHashMap()) { it.key to it.result }
}

/**
 * Walks every [ViewResult.Flat] leaf and invokes [action] with the path of bucket keys leading to it.
 * The path is `List<String?>` because each level can have a null bucket; the path entry at level `i` is
 * the bucket key under the i-th group-by axis.
 *
 * A [ViewResult.Flat] called directly invokes [action] with an empty path.
 */
fun ViewResult.walkLeaves(action: (path: List<String?>, properties: List<Property>) -> Unit) {
    walkLeavesImpl(emptyList(), action)
}

private fun ViewResult.walkLeavesImpl(
    path: List<String?>,
    action: (path: List<String?>, properties: List<Property>) -> Unit,
) {
    when (this) {
        is ViewResult.Flat    -> action(path, properties)
        is ViewResult.Grouped -> buckets.forEach { (k, v) -> v.walkLeavesImpl(path + k, action) }
    }
}

/**
 * Flat-map convenience: every leaf path → its properties. Iteration order is depth-first, matching
 * [walkLeaves]. The returned [LinkedHashMap] preserves that order.
 */
fun ViewResult.toFlatMap(): Map<List<String?>, List<Property>> {
    val out = LinkedHashMap<List<String?>, List<Property>>()
    walkLeaves { path, props -> out[path] = props }
    return out
}
