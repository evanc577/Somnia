package dev.evanchang.somnia.data

import android.text.Html
import androidx.annotation.Keep
import androidx.media3.common.MediaItem
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
    @SerialName("name")
    val id: String,
    val author: String,
    val subreddit: String,
    private val title: String,
    @SerialName("post_hint")
    val postHint: PostHint?,
    @SerialName("is_gallery")
    val isGallery: Boolean?,
    val url: String,
    private val preview: SubmissionPreview?,
    @SerialName("media_metadata")
    private val mediaMetadata: Map<String, MediaMetadata>?,
    @SerialName("secure_media")
    private val media: SecureMedia?,
    val score: Int,
    @SerialName("num_comments")
    val numComments: Int,
    private val created: Float,
) {
    fun escapedTitle(): String {
        return escapeString(title)
    }

    fun elapsedTime(): Duration {
        return (Instant.now().epochSecond - created).roundToInt().toDuration(DurationUnit.SECONDS)
    }

    fun elapsedTimeString(): String {
        val elapsedTime = elapsedTime()
        if (elapsedTime < (1).toDuration(DurationUnit.MINUTES)) {
            return "${elapsedTime.inWholeSeconds}s"
        } else if (elapsedTime < (1).toDuration(DurationUnit.HOURS)) {
            return "${elapsedTime.inWholeMinutes}m"
        } else if (elapsedTime < (1).toDuration(DurationUnit.DAYS)) {
            return "${elapsedTime.inWholeHours}h"
        } else if (elapsedTime < (365).toDuration(DurationUnit.DAYS)) {
            return "${elapsedTime.inWholeDays}d"
        } else {
            return "${elapsedTime.inWholeDays / 365}y"
        }
    }

    fun previewImage(): PreviewImage? {
        return if (preview != null) {
            preview.images[0].source
        } else if (mediaMetadata != null) {
            mediaMetadata.values.first().source
        } else {
            null
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

    private fun images(): List<String>? {
        if (postHint == PostHint.IMAGE) {
            return arrayListOf(url)
        } else if (isGallery == true) {
            return mediaMetadata?.map({ (k, _) -> "https://i.redd.it/${k}.jpg" })?.toList()
        }
        return null
    }

    private fun video(): MediaItem? {
        if (media?.redditVideo == null) {
            return null
        }
        val mediaItem = MediaItem.fromUri(media.redditVideo.hlsUrl)
        return mediaItem
    }
}

sealed class Media {
    class Images(val images: List<String>) : Media()
    class RedditVideo(val video: MediaItem) : Media()
}

@Keep
@Serializable
data class SubmissionPreview(
    val images: List<PreviewImages>,
)

@Keep
@Serializable
data class PreviewImages(
    val source: PreviewImage,
)

@Keep
@Serializable
data class PreviewImage(
    @SerialName(value = "url")
    @JsonNames("u")
    private val url: String,

    @SerialName(value = "width")
    @JsonNames("x")
    val width: Int,

    @SerialName(value = "height")
    @JsonNames("y")
    val height: Int,
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