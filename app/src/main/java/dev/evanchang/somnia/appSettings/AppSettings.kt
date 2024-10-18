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
    val redditApiClientId: String? = null,
    val redditUserAgent: String = "somnia",
)