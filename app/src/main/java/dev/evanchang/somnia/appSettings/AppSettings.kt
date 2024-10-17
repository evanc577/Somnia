package dev.evanchang.somnia.appSettings

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val redditApiClientId: String? = null,
    val redditUserAgent: String = "somnia",
)
