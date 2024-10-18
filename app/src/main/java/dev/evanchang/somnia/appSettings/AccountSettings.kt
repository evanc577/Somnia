package dev.evanchang.somnia.appSettings

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AccountSettings(
    val user: String,

    // Auth
    val refreshToken: String,
    val bearerToken: BearerToken,
)

@Keep
@Serializable
data class BearerToken(
    val token: String,
)
