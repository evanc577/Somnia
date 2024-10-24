package dev.evanchang.somnia.data

import android.text.Html
import androidx.annotation.Keep
import dev.evanchang.somnia.serializer.SerializableImmutableList
import dev.evanchang.somnia.serializer.SerializableImmutableMap
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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
    @SerialName("media_metadata") private val mediaMetadata: SerializableImmutableMap<String, MediaMetadata>?,
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
            return mediaMetadata?.map { (k, _) -> "https://i.redd.it/${k}.jpg" }?.toPersistentList()
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

@Keep
@Serializable
data class SubmissionPreview(
    val images: SerializableImmutableList<PreviewImages>,
)

@Keep
@Serializable
data class PreviewImages(
    val source: PreviewImage,
//    val resolutions: PersistentList<PreviewImage>,
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