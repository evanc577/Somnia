package dev.evanchang.somnia.ui.navigation

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

// Allows Screen to be dragged horizontally to the right
@Composable
fun HorizontalDraggableScreen(
    screenStackIndex: Int = 0,
    navigationViewModel: NavigationViewModel,
    content: @Composable () -> Unit,
) {
    val navigationUiState = navigationViewModel.navigationUIState.collectAsState()
    val screenWidth = navigationUiState.value.screenWidth
    val screenXOffset = navigationUiState.value.screenXOffset
    val navigationBackStack = navigationUiState.value.navigationBackStack
    val isTopScreen = screenStackIndex == navigationBackStack.lastIndex

    Box(modifier = Modifier
        .then(if (isTopScreen) {
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
        content()
    }
}