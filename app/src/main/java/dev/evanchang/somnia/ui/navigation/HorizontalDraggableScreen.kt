package dev.evanchang.somnia.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

// Allows Screen to be dragged horizontally to the right
@Composable
fun HorizontalDraggableScreen(
    screenStackIndex: Int = 0,
    navigationViewModel: NavigationViewModel,
    content: @Composable () -> Unit,
) {
    val navigationUiState = navigationViewModel.navigationUIState.collectAsStateWithLifecycle()
    val screenWidth = remember { navigationUiState.value.screenWidth }
    val screenXOffset = remember { navigationUiState.value.screenXOffset }
    val navigationBackStack = remember { navigationUiState.value.navigationBackStack }
    val isTopScreen =
        remember { derivedStateOf { screenStackIndex == navigationBackStack.lastIndex } }

    BackHandler(enabled = screenStackIndex != 0) {
        navigationViewModel.popBackStack()
    }

    Box(modifier = Modifier
        .then(if (isTopScreen.value) {
            Modifier.offset { IntOffset(x = screenXOffset.floatValue.roundToInt(), y = 0) }
        } else {
            Modifier.drawWithContent {
                drawContent()
                drawRect(
                    color = Color.Black.copy(
                        alpha = (screenWidth - screenXOffset.floatValue) / screenWidth * 0.8f
                    )
                )
            }
        })
        .draggable(enabled = (screenStackIndex != 0),
            orientation = Orientation.Horizontal,
            onDragStopped = {
                navigationViewModel.horizontalScreenDragEnded()
            },
            state = rememberDraggableState { delta ->
                navigationViewModel.updateScreenXOffset(delta)
            })
    ) {
        if (isTopScreen.value || screenXOffset.floatValue > 0) {
            content()
        }
    }
}