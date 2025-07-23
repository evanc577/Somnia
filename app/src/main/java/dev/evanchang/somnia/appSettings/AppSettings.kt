package dev.evanchang.somnia.appSettings

import androidx.annotation.Keep
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.serializer.SerializablePersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AppSettings(
    val activeUser: String? = null,
    val accountSettings: SerializablePersistentMap<String, AccountSettings> = persistentMapOf(),
    val generalSettings: GeneralSettings = GeneralSettings(),
    val apiSettings: ApiSettings = ApiSettings(),
)

@Keep
@Serializable
data class ApiSettings(
    val redditClientId: String? = null,
    val redditUserAgent: String = "somnia",
    val redditRedirectUri: String = "http://127.0.0.1",
)

@Keep
@Serializable
data class GeneralSettings(
    val defaultSubmissionSort: SubmissionSort = SubmissionSort.Best,
    val defaultCommentSort: CommentSort = CommentSort.BEST,
)