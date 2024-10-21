package dev.evanchang.somnia.ui.navigation

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
    content: @Composable (ColumnScope.() -> Unit),
) {

    val navigationUiState = navigationViewModel.navigationUIState.collectAsState()
    val navStackSize = navigationUiState.value.navigationBackStack.size
    val screenWidth = navigationUiState.value.screenWidth
//    val topScreenXOffset =
//        navigationUiState.value.navigationBackStack.lastOrNull()?.screenXOffset ?: return
//    val prevScreenXOffset =
//        navigationUiState.value.navigationBackStack.getOrNull(navStackSize - 2)?.screenXOffset ?: 0f
//
//    val animatedTopXOffset = animateFloatAsState(targetValue = topScreenXOffset)
//    val animatedPrevXOffset = animateFloatAsState(targetValue = prevScreenXOffset)
    val navigationBackStack = navigationUiState.value.navigationBackStack
    val isTopScreen = screenStackIndex == navigationBackStack.lastIndex

    Column(content = content, modifier = Modifier
        .offset {
            if (isTopScreen) {
                IntOffset(
                    navigationUiState.value.navigationBackStack.lastOrNull()?.screenXOffset?.value?.roundToInt()
                        ?: 0, 0
                )
            } else {
//                IntOffset(animatedPrevXOffset.value.roundToInt(), 0)
                IntOffset(0, 0)
            }
        }
        .then(if (!isTopScreen) {
            Modifier.drawWithContent {
                drawContent()
                drawRect(
                    color = Color.Black.copy(
                        alpha = ((screenWidth - (navigationUiState.value.navigationBackStack.lastOrNull()?.screenXOffset?.floatValue
                            ?: 0f))) / screenWidth * 0.8f
                    )
                )
            }
        } else {
            Modifier
        })
        .draggable(enabled = (screenStackIndex != 0),
            orientation = Orientation.Horizontal,
            onDragStopped = {
                navigationViewModel.horizontalScreenDragEnded()
            },
            state = rememberDraggableState { delta ->
                navigationViewModel.updateTopScreenXOffset(delta)
            })
    )

}