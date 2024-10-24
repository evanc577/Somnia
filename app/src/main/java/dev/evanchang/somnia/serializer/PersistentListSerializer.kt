package dev.evanchang.somnia.serializer

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias SerializablePersistentList<T> = @Serializable(PersistentListSerializer::class) PersistentList<T>

class PersistentListSerializer<T>(private val dataSerializer: KSerializer<T?>) :
    KSerializer<PersistentList<T>> {
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<T>) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): PersistentList<T> {
        return ListSerializer(dataSerializer).deserialize(decoder).filterNotNull()
            .toPersistentList()
    }
}
