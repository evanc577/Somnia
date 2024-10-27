package dev.evanchang.somnia.data

import android.text.Html
import android.webkit.MimeTypeMap
import androidx.annotation.Keep
import dev.evanchang.somnia.serializer.SerializableImmutableList
import dev.evanchang.somnia.serializer.SerializableImmutableMap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
    val name: String,
    val id: String,
    val author: String,
    val subreddit: String,
    private val title: String,
    val selftext: String,
    @SerialName("post_hint") val postHint: PostHint?,
    @SerialName("is_gallery") val isGallery: Boolean?,
    @SerialName("gallery_data") val galleryData: GalleryData?,
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
        val images = ArrayList(previewImages())
        images.sortByDescending {
            it.numPixels()
        }

        val candidate = images.find {
            it.numPixels() <= 1000 * 1000
        }

        if (candidate != null) {
            return candidate
        }

        if (images.isNotEmpty()) {
            return images.first()
        }

        return null
    }

    private fun previewImages(): List<PreviewImage> {
        val previewImages = preview?.images?.map { previewImages ->
            val images = arrayListOf(previewImages.source)
            images.addAll(previewImages.resolutions)
            images
        }?.flatten()
        val galleryPreviewImages = galleryData?.items?.map { item ->
            val metadata = mediaMetadata?.get(item.mediaId) ?: return@map null
            val images = arrayListOf(metadata.source)
            images.addAll(metadata.resolutions)
            images
        }?.filterNotNull()?.flatten()

        return sequenceOf(
            previewImages.orEmpty(),
            galleryPreviewImages.orEmpty(),
        ).flatten().toList()
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

    private fun images(): ImmutableList<String>? {
        if (postHint == PostHint.IMAGE) {
            return listOf(url).toImmutableList()
        }

        return galleryData?.items?.map { item ->
            val metadata = mediaMetadata?.get(item.mediaId) ?: return@map null
            val ext =
                MimeTypeMap.getSingleton().getExtensionFromMimeType(metadata.mimeType) ?: "jpg"
            "https://i.redd.it/${item.mediaId}.${ext}"
        }?.filterNotNull()?.toImmutableList()
    }

    private fun video(): String? {
        if (media?.redditVideo == null) {
            return null
        }
        return media.redditVideo.hlsUrl
    }
}

@Keep
@Serializable
sealed class Media {
    @Keep
    @Serializable
    class Images(val images: ImmutableList<String>) : Media()
    @Keep
    @Serializable
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
    val resolutions: SerializableImmutableList<PreviewImage>,
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

    fun numPixels(): Int {
        return width * height
    }
}

@Keep
@Serializable
data class MediaMetadata(
    @SerialName("s") val source: PreviewImage,
    @SerialName("p") val resolutions: SerializableImmutableList<PreviewImage>,
    @SerialName("m") val mimeType: String,
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

@Keep
@Serializable
data class GalleryData(
    val items: SerializableImmutableList<GalleryDataItem>,
)

@Keep
@Serializable
data class GalleryDataItem(
    @SerialName("media_id") val mediaId: String,
    @SerialName("id") val id: Int,
)