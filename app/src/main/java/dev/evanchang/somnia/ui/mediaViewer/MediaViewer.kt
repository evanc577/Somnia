package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.evanchang.somnia.data.Media
import dev.evanchang.somnia.data.Submission
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewer(
    submission: Submission,
    screenSize: Offset,
    onClose: () -> Unit,
) {
    val density = LocalDensity.current

    val media = remember { submission.media() }
    if (media == null) {
        onClose()
        return
    }

    // Anchored drag
    val anchors = DraggableAnchors {
        DragValue.Up at -screenSize.y
        DragValue.Center at 0f
        DragValue.Down at screenSize.y
    }
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Center,
            anchors = anchors,
            positionalThreshold = { with(density) { 100.dp.toPx() } },
            velocityThreshold = { with(density) { 200.dp.toPx() } },
            snapAnimationSpec = spring(),
            decayAnimationSpec = splineBasedDecay(density),
        )
    }
    LaunchedEffect(dragState.settledValue) {
        if (dragState.settledValue == DragValue.Up || dragState.settledValue == DragValue.Down) {
            onClose()
        }
    }

    DialogFullScreen(
        onDismissRequest = onClose,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        color = Color.Black.copy(alpha = 1f - abs(dragState.requireOffset() / screenSize.y)),
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .anchoredDraggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                    )
                    .offset {
                        IntOffset(
                            x = 0,
                            y = dragState
                                .requireOffset()
                                .roundToInt(),
                        )
                    },
            ) {
                when (media) {
                    is Media.Images -> GalleryViewer(media.images)
                    is Media.RedditVideo -> VideoViewer(media.video)
                }
            }
        }
    }
}

private enum class DragValue { Up, Center, Down }
