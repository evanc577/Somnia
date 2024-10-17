package dev.evanchang.somnia.Data

import android.text.Html
import androidx.annotation.Keep
import androidx.media3.common.MediaItem
import com.google.gson.annotations.SerializedName
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Keep
data class Submission(
    @SerializedName("name") val id: String,
    val author: String,
    val subreddit: String,
    private val title: String,
    @SerializedName("post_hint") val postHint: PostHint?,
    @SerializedName("is_gallery") val isGallery: Boolean?,
    val url: String,
    private val preview: SubmissionPreview?,
    @SerializedName("media_metadata") private val mediaMetadata: Map<String, MediaMetadata>?,
    @SerializedName("secure_media") private val media: SecureMedia?,
    val score: Int,
    @SerializedName("num_comments") val numComments: Int,
    private val created: Int,
) {
    fun escapedTitle(): String {
        return escapeString(title)
    }

    fun elapsedTime(): Duration {
        return (Instant.now().epochSecond - created).toDuration(DurationUnit.SECONDS)
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
data class SubmissionPreview(
    val images: List<PreviewImages>,
)

@Keep
data class PreviewImages(
    val source: PreviewImage,
)

@Keep
data class PreviewImage(
    @SerializedName(value = "url", alternate = ["u"]) private val url: String,
    @SerializedName(value = "width", alternate = ["x"]) val width: Int,
    @SerializedName(value = "height", alternate = ["y"]) val height: Int,
) {
    fun escapedUrl(): String {
        return escapeString(url)
    }
}

@Keep
data class MediaMetadata(
    @SerializedName("s") val source: PreviewImage,
)

@Keep
data class SecureMedia(
    @SerializedName("reddit_video") val redditVideo: RedditVideo?
)

@Keep
data class RedditVideo(
    @SerializedName("dash_url") val dashUrl: String,
    @SerializedName("hls_url") val hlsUrl: String,
    val duration: Int,
)

@Keep
enum class PostHint {
    @SerializedName("image")
    IMAGE,
}

private fun escapeString(s: String): String {
    return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString()
}