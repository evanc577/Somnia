package dev.evanchang.somnia.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionScreen
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditScreen
import dev.evanchang.somnia.ui.settings.screen.ApiSettingsScreen
import dev.evanchang.somnia.ui.settings.screen.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
sealed class Nav : NavKey {
    @Serializable
    sealed class Settings : Nav() {
        @Serializable
        data object TopLevel : Settings()

        @Serializable
        data object General : Settings()

        @Serializable
        data object Account : Settings()

        @Serializable
        data object Api : Settings()
    }

    @Serializable
    data class Subreddit(val subreddit: String) : Nav()

    @Serializable
    data class Submission(
        val initialSubmission: dev.evanchang.somnia.data.Submission,
        val submissionId: String,
    ) : Nav()

    @Serializable
    data class MediaViewer(val media: Media) : Nav()
}

@Composable
fun NavigationRoot(
    backStack: NavBackStack,
    appSettings: AppSettings,
) {
    NavDisplay(
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        backStack = backStack,
        entryProvider = { key ->
            when (key) {
                is Nav.Settings -> SettingsNav(
                        key = key,
                        backStack = backStack,
                    )

                is Nav.Subreddit -> NavEntry(key) {
                    SubredditScreen(
                        key.subreddit,
                        appSettings,
                        backStack = backStack,
                    )
                }

                is Nav.Submission -> NavEntry(key) {
                    SubmissionScreen(
                        initialSubmission = key.initialSubmission,
                        submissionId = key.submissionId,
                        appSettings = appSettings,
                        backStack = backStack,
                    )
                }

                is Nav.MediaViewer -> TODO()

                else -> throw RuntimeException("Invalid NavKey")
            }
        }
    )
}

private fun SettingsNav(
    key: Nav.Settings,
    backStack: NavBackStack,
): NavEntry<NavKey> {
        return when (key) {
            is Nav.Settings.TopLevel -> NavEntry(key) {
                SettingsScreen(backStack)
            }
            is Nav.Settings.Api -> NavEntry(key) {
                ApiSettingsScreen(backStack)
            }
            is Nav.Settings.Account -> TODO()
            is Nav.Settings.General -> TODO()
        }
    }