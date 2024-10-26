package dev.evanchang.somnia.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

// Allows Screen to be dragged horizontally to the right
@Composable
fun HorizontalDraggableScreen(
    screenStackIndex: Int,
    navigationViewModel: NavigationViewModel,
    content: @Composable () -> Unit,
) {
    val navigationUiState = navigationViewModel.navigationUIState
    val screenWidth by navigationViewModel.screenWidth
    var isDragging by remember { mutableStateOf(false) }
    // Where the finger is
    var targetXOffset by remember { mutableFloatStateOf(0f) }
    // Current animation offset
    val screenXOffset = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(isDragging, targetXOffset) {
        if (isDragging) {
            // Snap directly to finger so it doesn't feel laggy
            screenXOffset.snapTo(targetXOffset)
        } else if (targetXOffset > screenWidth / 3) {
            // Animate right
            screenXOffset.animateTo(targetValue = screenWidth)
        } else {
            // Animate left
            screenXOffset.animateTo(targetValue = 0f)
        }
    }
    val screenReset by remember {
        derivedStateOf { !isDragging && screenXOffset.value < 0.01f }
    }
    LaunchedEffect(screenReset) {
        if (screenReset) {
            navigationViewModel.renderSecondScreen.value = false
        }
    }
    val screenDismissed by remember {
        derivedStateOf { !isDragging && screenXOffset.value > screenWidth - 0.01f }
    }
    LaunchedEffect(screenDismissed) {
        if (screenDismissed) {
            navigationViewModel.popBackStack()
            screenXOffset.snapTo(targetValue = 0f)
        }
    }

    val isTopScreen by remember {
        derivedStateOf { screenStackIndex == navigationUiState.value.navigationBackStack.lastIndex }
    }
    val isSecondScreen by remember {
        derivedStateOf { screenStackIndex + 1 == navigationUiState.value.navigationBackStack.lastIndex }
    }
    val shouldRender by remember {
        derivedStateOf { isTopScreen || (isSecondScreen && navigationViewModel.renderSecondScreen.value) }
    }

    BackHandler(enabled = screenStackIndex != 0) {
        navigationViewModel.popBackStack()
    }

    // Slide in when created (except base screen)
    var initialVisibility by remember { mutableStateOf(screenStackIndex == 0) }
    LaunchedEffect(Unit) {
        initialVisibility = true
    }

    if (shouldRender) {
        AnimatedVisibility(visible = initialVisibility,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            Box(modifier = Modifier
                .then(if (isTopScreen) {
                    Modifier
                        .offset {
                            IntOffset(
                                x = screenXOffset.value.roundToInt(), y = 0
                            )
                        }
                        .drawBehind {
                            drawRect(
                                color = Color.Black.copy(
                                    alpha = 0.8f * ((screenWidth - screenXOffset.value) / screenWidth)
                                ),
                                topLeft = Offset(
                                    x = -screenXOffset.value, y = 0f
                                ),
                                size = Size(
                                    width = screenXOffset.value, height = this.size.height
                                ),
                            )
                        }
                } else {
                    Modifier
                })
                .draggable(enabled = (screenStackIndex != 0),
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        isDragging = false
                    },
                    state = rememberDraggableState { delta ->
                        if (!isDragging) {
                            // If drag just started, reset target offset to current animation offset
                            targetXOffset = screenXOffset.value
                            navigationViewModel.renderSecondScreen.value = true
                            isDragging = true
                        }
                        targetXOffset = if (targetXOffset + delta < 0) {
                            0f
                        } else {
                            targetXOffset + delta
                        }
                    })
            ) {
                content()
            }
        }
    }
}
