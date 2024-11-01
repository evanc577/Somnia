@file:OptIn(ExperimentalSerializationApi::class)

package dev.evanchang.somnia.api.reddit.dto

import androidx.annotation.Keep
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.More
import dev.evanchang.somnia.data.Submission
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Keep
@Serializable
@JsonClassDiscriminator("kind")
sealed class RedditResponse {
    @Keep
    @Serializable
    @SerialName("Listing")
    class Listing(@SerialName("data") val data: ListingData) : RedditResponse()
}

@Keep
@Serializable
class ListingData(val children: List<Thing>, val after: String?)

@Keep
@Serializable
@JsonClassDiscriminator("kind")
sealed class Thing {
    @Keep
    @Serializable
    @SerialName("t3")
    class SubmissionThing(@SerialName("data") val submission: Submission) : Thing()
    @Keep
    @Serializable
    @SerialName("t1")
    class CommentThing(@SerialName("data") val comment: Comment) : Thing()
    @Keep
    @Serializable
    @SerialName("more")
    class MoreThing(@SerialName("data") val more: More) : Thing()
}