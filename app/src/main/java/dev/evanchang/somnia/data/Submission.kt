package dev.evanchang.somnia.data

import android.text.Html
import androidx.annotation.Keep
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
    val url: String,
    val preview: SubmissionPreview?,
    @SerializedName("media_metadata") val mediaMetadata: Map<String, MediaMetadata>?,
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
enum class PostHint {
    @SerializedName("image")
    IMAGE,
}

private fun escapeString(s: String): String {
    return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString()
}