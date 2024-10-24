package dev.evanchang.somnia.serializer

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializablePersistentMap<T, U> = @Serializable(PersistentMapSerializer::class) PersistentMap<T, U>

class PersistentMapSerializer<T, U>(
    private val keySerializer: KSerializer<T?>,
    private val valueSerializer: KSerializer<U?>,
) : KSerializer<PersistentMap<T, U>> {
    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<String, String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()
    override fun serialize(encoder: Encoder, value: PersistentMap<T, U>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value.toMap())
    }

    override fun deserialize(decoder: Decoder): PersistentMap<T, U> {
        val x: List<Pair<T, U>> = MapSerializer(keySerializer, valueSerializer).deserialize(decoder)
            .filter { (k, v) -> k != null && v != null }.map { (k, v) -> Pair(k!!, v!!) }
        return x.associate { it.first to it.second }.toPersistentMap()
    }
}
