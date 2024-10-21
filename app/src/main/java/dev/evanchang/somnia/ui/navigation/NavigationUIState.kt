package dev.evanchang.somnia.ui.navigation

import androidx.annotation.Keep
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel
import kotlinx.serialization.Serializable

data class NavigationUIState(
    val navigationBackStack: SnapshotStateList<NavigationBackStackEntry> = mutableStateListOf(
        NavigationBackStackEntry(
            screen = AppScreen.SubredditScreen,
            viewModel = SubmissionsListViewModel(
                subreddit = "dreamcatcher",
                sort = SubmissionSort.New,
            ),
            screenXOffset = mutableFloatStateOf(0f),
        )
    ),

    //--Horizontal Draggable Screen offsets--
    val screenWidth: Float = 0f,
)

data class NavigationBackStackEntry(
    val screen: AppScreen,
    val viewModel: ViewModel,

    var screenXOffset: MutableFloatState = mutableFloatStateOf(0f),
)


@Keep
@Serializable
sealed class AppScreen {
    @Keep
    @Serializable
    object SubredditScreen : AppScreen()

    @Keep
    @Serializable
    object CommentScreen : AppScreen()
}