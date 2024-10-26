package dev.evanchang.somnia.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.evanchang.somnia.ui.util.thenIf
import kotlin.math.roundToInt

// Allows Screen to be dragged horizontally to the right
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalDraggableScreen(
    screenStackIndex: Int,
    navigationViewModel: NavigationViewModel,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    val navigationUiState = navigationViewModel.navigationUIState
    val screenWidth by navigationViewModel.screenWidth

    val isTopScreen by remember {
        derivedStateOf { screenStackIndex == navigationUiState.value.navigationBackStack.lastIndex }
    }
    val isSecondScreen by remember {
        derivedStateOf { screenStackIndex + 1 == navigationUiState.value.navigationBackStack.lastIndex }
    }
    val shouldRender by remember {
        derivedStateOf { isTopScreen || isSecondScreen }
    }

    BackHandler(enabled = screenStackIndex != 0) {
        navigationViewModel.popBackStack()
    }

    // Slide in when created (except base screen)
    var initialVisibility by remember { mutableStateOf(screenStackIndex == 0) }
    LaunchedEffect(Unit) {
        initialVisibility = true
    }

    // Anchored drag
    val anchors = DraggableAnchors {
        DragValue.Start at 0f
        DragValue.End at screenWidth
    }
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Start,
            anchors = anchors,
            positionalThreshold = { totalDistance: Float -> totalDistance * 0.3f },
            velocityThreshold = { with(density) { 200.dp.toPx() } },
            snapAnimationSpec = spring(),
            decayAnimationSpec = splineBasedDecay(density),
        )
    }
    LaunchedEffect(dragState.settledValue) {
        if (dragState.settledValue == DragValue.End) {
            navigationViewModel.popBackStack()
        }
    }

    if (shouldRender) {
        AnimatedVisibility(
            visible = initialVisibility,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            Box(modifier = Modifier.thenIf(isTopScreen) {
                Modifier
                    .anchoredDraggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                    )
                    .offset {
                        IntOffset(
                            x = dragState
                                .requireOffset()
                                .roundToInt(), y = 0
                        )
                    }
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(
                                alpha = 0.8f * ((screenWidth - dragState.requireOffset()) / screenWidth)
                            ),
                            topLeft = Offset(
                                x = -dragState.requireOffset(), y = 0f
                            ),
                            size = Size(
                                width = dragState.requireOffset(), height = this.size.height
                            ),
                        )
                    }
            }) {
                content()
            }
        }
    }
}

enum class DragValue { Start, End }