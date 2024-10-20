package dev.evanchang.somnia.appSettings

import androidx.annotation.Keep
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AppSettings(
    val activeUser: String? = null,
    @Serializable(with = MyPersistentMapSerializer::class)
    val accountSettings: PersistentMap<String, AccountSettings> = persistentMapOf(),
    val apiSettings: ApiSettings = ApiSettings(),
)

@Keep
@Serializable
data class ApiSettings(
    val redditClientId: String? = null,
    val redditUserAgent: String = "somnia",
    val redditRedirectUri: String = "http://127.0.0.1",
)