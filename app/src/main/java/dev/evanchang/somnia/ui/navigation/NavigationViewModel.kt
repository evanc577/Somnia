package dev.evanchang.somnia.ui.navigation

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel : ViewModel() {
    private val _navigationUIState = MutableStateFlow(NavigationUIState())
    val navigationUIState = _navigationUIState.asStateFlow()
    val screenWidth = mutableFloatStateOf(0f)
    val renderSecondScreen = mutableStateOf(false)

    fun setScreenWidth(width: Float) {
        screenWidth.floatValue = width
    }

    fun pushSubredditScreen(viewModel: SubredditViewModel) {
        _navigationUIState.value.navigationBackStack.add(
            NavigationBackStackEntry.SubredditBackStackEntry(
                viewModel = viewModel
            )
        )
    }

    fun pushSubmissionScreen(viewModel: SubmissionViewModel) {
        _navigationUIState.value.navigationBackStack.add(
            NavigationBackStackEntry.SubmissionBackStackEntry(
                viewModel = viewModel
            )
        )
    }

    fun popBackStack() {
        _navigationUIState.value.navigationBackStack.removeLastOrNull()
    }
}