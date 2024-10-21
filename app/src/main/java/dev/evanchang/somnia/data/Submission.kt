package dev.evanchang.somnia.data

import android.text.Html
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonNames
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = PersistentMap::class)
class MediaMetadataPersistentMapSerializer(
    private val keySerializer: KSerializer<String?>,
    private val valueSerializer: KSerializer<MediaMetadata?>,
) : KSerializer<PersistentMap<String, MediaMetadata>> {
    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<String, MediaMetadata>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentMap"
    }

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()
    override fun serialize(encoder: Encoder, value: PersistentMap<String, MediaMetadata>) {
        return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value.toMap())
    }

    override fun deserialize(decoder: Decoder): PersistentMap<String, MediaMetadata> {
        return MapSerializer(keySerializer, valueSerializer).deserialize(decoder)
            .filter { (k, v) -> k != null && v != null }.map { (k, v) -> Pair(k!!, v!!) }
            .associate { it.first to it.second }.toPersistentMap()
    }
}

@Keep
@Serializable
data class Submission(
    @SerialName("name") val id: String,
    val author: String,
    val subreddit: String,
    private val title: String,
    @SerialName("post_hint") val postHint: PostHint?,
    @SerialName("is_gallery") val isGallery: Boolean?,
    val url: String,
    private val preview: SubmissionPreview?,
    @Serializable(MediaMetadataPersistentMapSerializer::class) @SerialName("media_metadata") private val mediaMetadata: PersistentMap<String, MediaMetadata>?,
    @SerialName("secure_media") private val media: SecureMedia?,
    val score: Int,
    @SerialName("num_comments") val numComments: Int,
    private val created: Float,
) {
    fun escapedTitle(): String {
        return escapeString(title)
    }

    private fun elapsedTime(): Duration {
        return (Instant.now().epochSecond - created).roundToInt().toDuration(DurationUnit.SECONDS)
    }

    fun elapsedTimeString(): String {
        val elapsedTime = elapsedTime()
        return if (elapsedTime < (1).toDuration(DurationUnit.MINUTES)) {
            "${elapsedTime.inWholeSeconds}s"
        } else if (elapsedTime < (1).toDuration(DurationUnit.HOURS)) {
            "${elapsedTime.inWholeMinutes}m"
        } else if (elapsedTime < (1).toDuration(DurationUnit.DAYS)) {
            "${elapsedTime.inWholeHours}h"
        } else if (elapsedTime < (365).toDuration(DurationUnit.DAYS)) {
            "${elapsedTime.inWholeDays}d"
        } else {
            "${elapsedTime.inWholeDays / 365}y"
        }
    }

    fun previewImage(): PreviewImage? {
        return if (preview != null) {
            preview.images[0].source
        } else {
            mediaMetadata?.values?.firstOrNull()?.source
        }
    }

    fun media(): Media? {
        val images = images()
        if (images != null) {
            return Media.Images(images)
        }

        val video = video()
        if (video != null) {
            return Media.RedditVideo(video)
        }

        return null
    }

    private fun images(): PersistentList<String>? {
        if (postHint == PostHint.IMAGE) {
            return persistentListOf(url)
        } else if (isGallery == true) {
            return mediaMetadata?.map { (k, _) -> "https://i.redd.it/${k}.jpg" }
                ?.toPersistentList()
        }
        return null
    }

    private fun video(): String? {
        if (media?.redditVideo == null) {
            return null
        }
        return media.redditVideo.hlsUrl
    }
}

sealed class Media {
    class Images(val images: PersistentList<String>) : Media()
    class RedditVideo(val video: String) : Media()
}

@Suppress("EXTERNAL_SERIALIZER_USELESS")
@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = PersistentList::class)
class PreviewImagePersistentListSerializer(private val dataSerializer: KSerializer<PreviewImage?>) :
    KSerializer<PersistentList<PreviewImage>> {
    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<PreviewImage>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<PreviewImage>) {
        return ListSerializer(dataSerializer).serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): PersistentList<PreviewImage> {
        return ListSerializer(dataSerializer).deserialize(decoder).filterNotNull()
            .toPersistentList()
    }
}

@Keep
@Serializable
data class SubmissionPreview(
    @Serializable(with = PreviewImagePersistentListSerializer::class) val images: PersistentList<PreviewImages>,
)

@Keep
@Serializable
data class PreviewImages(
    val source: PreviewImage,
)

@Keep
@Serializable
data class PreviewImage @OptIn(ExperimentalSerializationApi::class) constructor(
    @SerialName(value = "url") @JsonNames("u") private val url: String,

    @SerialName(value = "width") @JsonNames("x") val width: Int,

    @SerialName(value = "height") @JsonNames("y") val height: Int,
) {
    fun escapedUrl(): String {
        return escapeString(url)
    }
}

@Keep
@Serializable
data class MediaMetadata(
    @SerialName("s") val source: PreviewImage,
)

@Keep
@Serializable
data class SecureMedia(
    @SerialName("reddit_video") val redditVideo: RedditVideo?
)

@Keep
@Serializable
data class RedditVideo(
    @SerialName("dash_url") val dashUrl: String,
    @SerialName("hls_url") val hlsUrl: String,
    val duration: Int,
)

@Keep
@Serializable
enum class PostHint {
    @SerialName("image")
    IMAGE,
}

private fun escapeString(s: String): String {
    return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString()
}