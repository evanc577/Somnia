package dev.evanchang.somnia.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionScreen
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditScreen
import dev.evanchang.somnia.ui.settings.screen.AccountSettingsNavKey
import dev.evanchang.somnia.ui.settings.screen.SettingsNav
import dev.evanchang.somnia.ui.settings.screen.SettingsNavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Nav : NavKey {
    @Serializable
    data class Settings(val key: SettingsNavKey) : Nav()

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
    appSettings: AppSettings,
) {
    val backStack = rememberNavBackStack(
        Nav.Subreddit(
            subreddit = "",
        )
    )

    val onBack: (Int) -> Unit = { count ->
        repeat(count) { backStack.removeLastOrNull() }
    }
    val onNavigate: (Nav) -> Unit = { nav ->
        backStack.add(nav)
    }

    NavDisplay(
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        backStack = backStack,
        onBack = onBack,
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = { key ->
            when (key) {
                is Nav.Settings -> SettingsNav(
                    key = key.key,
                    onBack = onBack,
                    onNavigate = onNavigate,
                )

                is Nav.Subreddit -> NavEntry(key) {
                    SubredditScreen(
                        subreddit = key.subreddit,
                        appSettings = appSettings,
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

                is Nav.MediaViewer -> NavEntry(key) {
                    MediaViewer(
                        media = key.media,
                        onClose = { backStack.removeLastOrNull() }
                    )
                }

                else -> throw RuntimeException("Invalid NavKey ${key}")
            }
        }
    )
}