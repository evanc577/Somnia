package dev.evanchang.somnia.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {
    private val _navigationState = MutableStateFlow(NavigationUIState())
    val navigationUIState = _navigationState.asStateFlow()

    //-- Set value of screenXOffset and prevScreenXOffset --
    fun setScreenXOffset(offset: Float) {
        _navigationState.update { state ->
            state.copy(
                screenXOffset = offset, prevScreenXOffset = -offset / 8
            )
        }
    }

    //-- Push new screen on stack --
    fun pushToBackStack(screenRoute: AppScreen, viewModel: ViewModel) {
        _navigationState.value.navigationBackStack.add(
            Pair(screenRoute, viewModel)
        )
    }

    //-- Pop top of stack --
    private fun popBackStack() {
        val currentStack = _navigationState.value.navigationBackStack
        navigationUIState.value.navigationBackStack.removeAt(currentStack.lastIndex)
    }

    //-- Update XOffset for top and previous Screen --
    fun updateTopScreenXOffset(delta: Float) {
        val topScreenXOffset = _navigationState.value.topScreenXOffset
        val prevXOffset = _navigationState.value.prevScreenXOffset
        if (topScreenXOffset + delta >= 0.0f) {
            _navigationState.update { state ->
                state.copy(
                    topScreenXOffset = topScreenXOffset + delta,
                    prevScreenXOffset = prevXOffset + delta / 8
                )
            }
        }
    }

    //-- Reset values for Top and previous screen XOffsets --
    fun resetScreenXOffset() {
        val screenXOffset = _navigationState.value.screenXOffset
        _navigationState.update { state ->
            state.copy(
                topScreenXOffset = 0.0f,
                prevScreenXOffset = -screenXOffset / 8,
                prevScreenIndex = -1,
            )
        }
    }

    //-- Clean Up Top Screen XOffset when composition is destroyed --
    fun cleanUpXOffset() {
        _navigationState.update { state ->
            state.copy(
                topScreenXOffset = 0.0f,
            )
        }
    }

    //-- Set the Index of the prevScreen --
    private fun setPrevScreenIndex() {
        val nextPrevScreenIndex = _navigationState.value.navigationBackStack.lastIndex - 1
        _navigationState.update { state ->
            state.copy(
                prevScreenXOffset = 0.0f, prevScreenIndex = nextPrevScreenIndex
            )
        }
    }

    //-- CallBack for when a screen drag ends --
    fun horizontalScreenDragEnded(xBreakPoint: Float) {
        val screenXOffset = _navigationState.value.screenXOffset
        val topScreenXOffset = _navigationState.value.topScreenXOffset
        if (topScreenXOffset > xBreakPoint) {
            setPrevScreenIndex()
            popBackStack()
        } else {
            _navigationState.update { state ->
                state.copy(
                    topScreenXOffset = 0.0f,
                    prevScreenXOffset = -screenXOffset / 8f,
                )
            }
        }
    }
}