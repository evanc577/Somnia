package dev.evanchang.somnia.appSettings

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AppSettings(
    val redditApiClientId: String? = null,
    val redditUserAgent: String = "somnia",
)