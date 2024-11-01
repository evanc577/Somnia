package dev.evanchang.somnia.data

import androidx.annotation.Keep
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.serializer.SerializableImmutableList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Keep
@Serializable
data class Comment(
    val name: String,
    val id: String,
    val author: String,
    val permalink: String,
    val body: String,
    val score: Int,
    @SerialName("score_hidden") val scoreHidden: Boolean,
    private val created: Float,
    val depth: Int,
    @Serializable(with = NullableRedditResponseSerializer::class) val replies: RedditResponse?,
)

@Keep
@Serializable
data class More(
    val name: String,
    val id: String,
    val depth: Int,
    val children: SerializableImmutableList<String>,
)

// Reddit returns null replies as an empty string, catch parse errors during decoding and convert
// them to null instead.
class NullableRedditResponseSerializer : KSerializer<RedditResponse?> {
    private class RedditResponseDescriptor : SerialDescriptor by serialDescriptor<List<String>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "dev.evanchang.somnia.redditresponse"
    }

    override val descriptor: SerialDescriptor = RedditResponseDescriptor()
    private val delegate = RedditResponse.serializer().nullable

    override fun serialize(encoder: Encoder, value: RedditResponse?) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): RedditResponse? {
        return try {
            delegate.deserialize(decoder)
        } catch (e: Exception) {
            null
        }
    }
}