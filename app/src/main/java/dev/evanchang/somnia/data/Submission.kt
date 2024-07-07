package dev.evanchang.somnia.data

import android.text.Html
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

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
) {
    fun escapedTitle(): String {
        return escapeString(title)
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