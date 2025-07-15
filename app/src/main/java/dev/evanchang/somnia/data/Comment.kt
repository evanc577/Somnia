package dev.evanchang.somnia.data

import androidx.annotation.Keep
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.serializer.SerializableImmutableList
import dev.evanchang.somnia.serializer.SerializablePersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Keep
@Serializable
sealed class Comment : CommentInterface {
    @Keep
    @Serializable
    data class CommentData(
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
    ) : Comment(), ElapsedTime {
        override fun elapsedTime(): Duration {
            return (Instant.now().epochSecond - created).roundToInt()
                .toDuration(DurationUnit.SECONDS)
        }

        override fun name(): String {
            return name
        }

        override fun id(): String {
            return id
        }

        override fun depth(): Int {
            return depth
        }
    }

    @Keep
    @Serializable
    data class More(
        val name: String,
        val id: String,
        val depth: Int,
        var children: SerializablePersistentList<String>,
    ) : Comment() {
        override fun name(): String {
            return "more_${name}"
        }

        override fun id(): String {
            return id
        }

        override fun depth(): Int {
            return depth
        }
    }
}

// Reddit returns null replies as an empty string, catch parse errors during decoding and convert
// them to null instead.
class NullableRedditResponseSerializer : KSerializer<RedditResponse?> {
    @OptIn(SealedSerializationApi::class)
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

private interface CommentInterface {
    fun name(): String
    fun id(): String
    fun depth(): Int
}

fun List<Comment>.flatten(): Sequence<Comment> {
    return sequence {
        for (comment in this@flatten) {
            yield(comment)
            when (comment) {
                is Comment.CommentData -> if (comment.replies != null && comment.replies is RedditResponse.Listing) {
                    yieldAll(
                        comment.replies.data.children.filterIsInstance<Thing.CommentThing>()
                            .map { ct -> ct.comment }.flatten()
                    )
                }

                is Comment.More -> {}
            }
        }
    }
}