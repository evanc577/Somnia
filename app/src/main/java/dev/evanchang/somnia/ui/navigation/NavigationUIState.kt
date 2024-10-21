package dev.evanchang.somnia.ui.navigation

import androidx.annotation.Keep
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel
import kotlinx.serialization.Serializable

data class NavigationUIState(
    //--Back Stack for navigation--
    val navigationBackStack: SnapshotStateList<Pair<AppScreen, ViewModel>> = mutableStateListOf(
        Pair(
            first = AppScreen.SubredditScreen,
            second = SubmissionsListViewModel(
                subreddit = "dreamcatcher",
                sort = SubmissionSort.New,
            ),
        )
    ),

    //--Horizontal Draggable Screen offsets--
    val screenXOffset: Float = 0.0f,
    val topScreenXOffset: Float = 0.0f,
    val prevScreenXOffset: Float = 0.0f,

    //-- Index of previous screen, this is used to fine tune animations.--
    val prevScreenIndex: Int = -1,
)

data class NavigationBackStackEntry(
    val screen: AppScreen,
    val viewModel: ViewModel,
    
    val screenXOffset: Float = 0.0f,
    val topScreenXOffset: Float = 0.0f,
    val prevScreenXOffset: Float = 0.0f,
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