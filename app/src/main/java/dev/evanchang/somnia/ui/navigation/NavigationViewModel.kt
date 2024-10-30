package dev.evanchang.somnia.ui.navigation

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel(private val appSettings: AppSettings) : ViewModel() {
    private val _navigationUIState =
        MutableStateFlow(NavigationUIState(appSettings.generalSettings.defaultSubmissionSort))
    val navigationUIState = _navigationUIState.asStateFlow()
    val screenSize = mutableStateOf(Offset(0f, 0f))

    fun setScreenSize(offset: Offset) {
        screenSize.value = offset
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

@Suppress("UNCHECKED_CAST")
class NavigationViewModelFactory(private val appSettings: AppSettings) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NavigationViewModel(appSettings) as T
}