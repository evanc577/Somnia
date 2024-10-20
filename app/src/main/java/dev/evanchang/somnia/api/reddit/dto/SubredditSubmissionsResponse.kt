package dev.evanchang.somnia.api.reddit.dto

import androidx.annotation.Keep
import dev.evanchang.somnia.data.Submission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SubredditSubmissionsResponse(
    @SerialName("data") val responseData: SubredditSubmissionsResponseData,
) {
    @Keep
    @Serializable
    data class SubredditSubmissionsResponseData(
        @SerialName("after") val after: String?,
        @SerialName("children") val children: List<SubredditSubmissionsResponseSubmissionWrapper>,
    ) {
        @Keep
        @Serializable
        data class SubredditSubmissionsResponseSubmissionWrapper(
            @SerialName("data") val submission: Submission,
        )
    }
}
