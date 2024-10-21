package dev.evanchang.somnia.ui.navigation

import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {
    private val _navigationUIState = MutableStateFlow(NavigationUIState())
    val navigationUIState = _navigationUIState.asStateFlow()

    private val prevScreenOffsetMultiplier = 8f

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
                screenXOffset = mutableFloatStateOf(0f),
            )
        )
    }

    private fun popBackStack() {
        _navigationUIState.value.navigationBackStack.removeLastOrNull()
        _navigationUIState.value.navigationBackStack.lastOrNull()?.screenXOffset?.value = 0f
    }

    fun updateTopScreenXOffset(delta: Float) {
        val stackSize = navigationUIState.value.navigationBackStack.size

        // Update top screen
        val topScreen =
            _navigationUIState.value.navigationBackStack.getOrNull(stackSize - 1) ?: return
        topScreen.screenXOffset.value += delta

        // Update prev screen
        if (stackSize < 2) {
            return
        }
        val prevScreen =
            _navigationUIState.value.navigationBackStack.getOrNull(stackSize - 2) ?: return
        prevScreen.screenXOffset.value += delta / prevScreenOffsetMultiplier
    }

    fun horizontalScreenDragEnded() {
        val screenWidth = _navigationUIState.value.screenWidth
        val topScreen = _navigationUIState.value.navigationBackStack.lastOrNull() ?: return
        if (topScreen.screenXOffset.value > screenWidth / 3) {
            popBackStack()
        } else {
            // Reset the drag position
            topScreen.screenXOffset.value = 0f
            val stackSize = _navigationUIState.value.navigationBackStack.size
            val prevScreen =
                _navigationUIState.value.navigationBackStack.getOrNull(stackSize - 2) ?: return
            prevScreen.screenXOffset.value = -screenWidth / prevScreenOffsetMultiplier
        }
    }
}