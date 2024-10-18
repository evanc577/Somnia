package dev.evanchang.somnia.appSettings

import androidx.annotation.Keep
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AppSettings(
    val accountSettings: PersistentList<AccountSettings> = persistentListOf(),
    val apiSettings: ApiSettings = ApiSettings(),
)

@Keep
@Serializable
data class ApiSettings(
    val redditClientId: String? = null,
    val redditUserAgent: String = "somnia",
    val redditRedirectUri: String = "http://127.0.0.1",
)