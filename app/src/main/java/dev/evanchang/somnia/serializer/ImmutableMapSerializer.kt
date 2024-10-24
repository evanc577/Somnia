package dev.evanchang.somnia.serializer

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializableImmutableMap<T, U> = @Serializable(ImmutableMapSerializer::class) ImmutableMap<T, U>

class ImmutableMapSerializer<T, U>(
    private val keySerializer: KSerializer<T?>,
    private val valueSerializer: KSerializer<U?>,
) : KSerializer<ImmutableMap<T, U>> {
    private class ImmutableMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<String, String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.immutableMap"
    }

    override val descriptor: SerialDescriptor = ImmutableMapDescriptor()
    override fun serialize(encoder: Encoder, value: ImmutableMap<T, U>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value.toMap())
    }

    override fun deserialize(decoder: Decoder): ImmutableMap<T, U> {
        val x: List<Pair<T, U>> = MapSerializer(keySerializer, valueSerializer).deserialize(decoder)
            .filter { (k, v) -> k != null && v != null }.map { (k, v) -> Pair(k!!, v!!) }
        return x.associate { it.first to it.second }.toImmutableMap()
    }
}
