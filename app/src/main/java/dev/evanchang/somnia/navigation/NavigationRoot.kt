package dev.evanchang.somnia.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionScreen
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditScreen
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

data class Navigation(
    val backStack: NavBackStack = NavBackStack(),
    val onBack: (Int) -> Unit = { repeat(it) { backStack.removeLastOrNull() } },
    val onNavigate: (Nav) -> Unit = { backStack.add(it) },
)

val LocalNavigation = staticCompositionLocalOf { Navigation() }

@Composable
fun NavigationRoot() {
    val navigation = Navigation(backStack = rememberNavBackStack(Nav.Subreddit("")))
    CompositionLocalProvider(LocalNavigation provides navigation) {
        NavDisplay(
            entryDecorators = listOf(
                rememberSceneSetupNavEntryDecorator(),
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            backStack = navigation.backStack,
            onBack = navigation.onBack,
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
                    )

                    is Nav.Subreddit -> NavEntry(key) {
                        SubredditScreen(
                            subreddit = key.subreddit,
                        )
                    }

                    is Nav.Submission -> NavEntry(key) {
                        SubmissionScreen(
                            initialSubmission = key.initialSubmission,
                            submissionId = key.submissionId,
                        )
                    }

                    is Nav.MediaViewer -> NavEntry(
                        key = key,
                        metadata = NavDisplay.transitionSpec {
                            EnterTransition.None togetherWith ExitTransition.KeepUntilTransitionsFinished
                        } + NavDisplay.popTransitionSpec {
                            EnterTransition.None togetherWith slideOutVertically(targetOffsetY = { -it })
                        } + NavDisplay.popTransitionSpec {
                            EnterTransition.None togetherWith slideOutVertically(targetOffsetY = { -it })
                        },
                    ) {
                        MediaViewer(
                            media = key.media,
                            onClose = { navigation.onBack(1) },
                        )
                    }

                    else -> throw RuntimeException("Invalid NavKey ${key}")
                }
            }
        )
    }
}