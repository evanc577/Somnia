package dev.evanchang.somnia.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {
    private val _navigationUIState = MutableStateFlow(NavigationUIState())
    val navigationUIState = _navigationUIState.asStateFlow()

    fun setScreenWidth(width: Float) {
        _navigationUIState.update { state ->
            state.copy(screenWidth = width)
        }
    }

    fun pushToBackStack(screen: AppScreen, viewModel: ViewModel) {
        _navigationUIState.value.navigationBackStack.add(
            NavigationBackStackEntry(
                screen = screen,
                viewModel = viewModel,
            )
        )
        _navigationUIState.value.screenXOffset.floatValue = 0f
    }

    fun popBackStack() {
        _navigationUIState.value.navigationBackStack.removeLastOrNull()
        _navigationUIState.value.screenXOffset.floatValue = 0f
    }

    fun updateScreenXOffset(delta: Float) {
        if (_navigationUIState.value.screenXOffset.floatValue + delta < 0) {
            _navigationUIState.value.screenXOffset.floatValue = 0f
        } else {
            _navigationUIState.value.screenXOffset.floatValue += delta
        }
    }

    fun horizontalScreenDragEnded() {
        val screenWidth = _navigationUIState.value.screenWidth
        if (_navigationUIState.value.screenXOffset.floatValue > screenWidth / 3) {
            popBackStack()
        } else {
            // Reset the drag position
            _navigationUIState.value.screenXOffset.floatValue = 0f
        }
    }
}