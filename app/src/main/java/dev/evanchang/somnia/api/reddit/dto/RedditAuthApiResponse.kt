package dev.evanchang.somnia.api.reddit.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RedditAuthApiResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("scope") val scope: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Keep
@Serializable
data class RedditApiMeResponse(
    val name: String,
)