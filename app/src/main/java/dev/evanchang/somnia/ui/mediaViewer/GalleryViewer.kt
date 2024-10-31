package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.maxBitmapSize
import coil3.size.Dimension
import coil3.size.Size
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE
import dev.evanchang.somnia.ui.util.ImageLoading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun GalleryViewer(
    images: ImmutableList<String>,
) {
    // Pager
    val pagerState = rememberPagerState { images.size }

    // Images
    val context = LocalPlatformContext.current
    val zoomListener = remember { DoubleClickToZoomListener.cycle(maxZoomFactor = 3f) }
    val painters = images.map { image ->
        rememberAsyncImagePainter(model = remember {
            ImageRequest.Builder(context).data(image).crossfade(true)
                .maxBitmapSize(Size(Dimension.Undefined, Dimension.Undefined)).build()
        })
    }.toImmutableList()
    val states = painters.map { painter ->
        painter.state.collectAsStateWithLifecycle(context)
    }.toImmutableList()
    val zoomableStates =
        images.map { _ -> rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 50f)) }
            .toImmutableList()
    // Set zoom state edge detection after image has loaded
    for ((state, zoomableState) in states.zip(zoomableStates)) {
        LaunchedEffect(state.value) {
            if (state.value is AsyncImagePainter.State.Success) {
                val image = (state.value as AsyncImagePainter.State.Success).result.image
                zoomableState.setContentLocation(
                    ZoomableContentLocation.scaledToFitAndCenterAligned(
                        androidx.compose.ui.geometry.Size(
                            image.width.toFloat(),
                            image.height.toFloat()
                        )
                    )
                )
            }
        }
    }

    // Header
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.safeDrawing.getTop(density).toDp() }

    // Immersive view toggle
    var immersive by rememberSaveable { mutableStateOf(false) }
    val view = LocalView.current
    LaunchedEffect(immersive) {
        if (immersive) {
            view.windowInsetsController?.hide(
                android.view.WindowInsets.Type.systemBars()
            )
        } else {
            view.windowInsetsController?.show(
                android.view.WindowInsets.Type.systemBars()
            )
        }
    }

    ConstraintLayout {
        val (header, image) = createRefs()

        HorizontalPager(
            state = pagerState, beyondViewportPageCount = 1,
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .clickable {
                    immersive = !immersive
                },
        ) { idx ->
            when (states[idx].value) {
                is AsyncImagePainter.State.Success -> {
                    Image(
                        painter = painters[idx],
                        contentDescription = "gallery image ${idx + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .zoomable(state = zoomableStates[idx],
                                onDoubleClick = zoomListener,
                                onClick = { immersive = !immersive }),
                    )
                }

                is AsyncImagePainter.State.Loading -> {
                    ImageLoading(color = Color.White)
                }

                is AsyncImagePainter.State.Error -> {
                    ImageError(onRetry = { painters[idx].restart() })
                }

                is AsyncImagePainter.State.Empty -> {}
            }
        }

        AnimatedVisibility(visible = !immersive, enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = Color.Black.copy(alpha = 0.3f))
                    }
                    .constrainAs(header) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    },
            ) {
                Spacer(modifier = Modifier.height(statusBarHeight))
                Text(
                    "Image ${pagerState.currentPage + 1}/${images.size}",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(CARD_PADDING),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ImageError(onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = "Image failed to load",
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(SPACER_SIZE))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(text = "Retry")
        }
    }
}

@Preview
@Composable
private fun PreviewGalleryViewer() {
    val images = listOf("https://i.redd.it/yrjzzr5st6ud1.jpeg").toImmutableList()
    GalleryViewer(images = images)
}