package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.evanchang.somnia.api.media.MediaItem
import dev.evanchang.somnia.api.media.MediaType
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.util.MediaError
import dev.evanchang.somnia.ui.util.MediaLoading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun GalleryViewer(
    mediaItems: ImmutableList<MediaItem>,
) {
    // Pager
    val pagerState = rememberPagerState { mediaItems.size }

    val context = LocalPlatformContext.current
    val zoomListener = remember { DoubleClickToZoomListener.cycle(maxZoomFactor = 3f) }

    // Create list of MediaData, containing required state for either an image or video
    val mediaData = mediaItems.map { mediaItem ->
        when (mediaItem.mediaType) {
            MediaType.Video -> MediaData.Video(url = mediaItem.url)
            MediaType.Image -> {
                val painter = rememberAsyncImagePainter(model = remember {
                    ImageRequest.Builder(context).data(mediaItem.url).crossfade(true).build()
                })
                val state = painter.state.collectAsStateWithLifecycle(context)
                val zoomableState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 50f))
                MediaData.Image(
                    painter = painter,
                    state = state,
                    zoomableState = zoomableState,
                )
            }
        }
    }.toImmutableList()

    // Set zoom state edge detection after image has loaded
    for (md in mediaData) {
        if (md is MediaData.Image) {
            LaunchedEffect(md.state.value) {
                if (md.state.value is AsyncImagePainter.State.Success) {
                    val image = (md.state.value as AsyncImagePainter.State.Success).result.image
                    md.zoomableState.setContentLocation(
                        ZoomableContentLocation.scaledToFitAndCenterAligned(
                            Size(
                                image.width.toFloat(), image.height.toFloat()
                            )
                        )
                    )
                }
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
            when (val md = mediaData[idx]) {
                is MediaData.Image -> when (md.state.value) {
                    is AsyncImagePainter.State.Success -> {
                        Image(
                            md.painter, "gallery image ${idx + 1}",
                            Modifier
                                .fillMaxSize()
                                .zoomable(
                                    state = md.zoomableState,
                                    onDoubleClick = zoomListener,
                                    onClick = { immersive = !immersive }),
                            contentScale = ContentScale.Fit,
                        )
                    }

                    is AsyncImagePainter.State.Loading -> {
                        MediaLoading(color = Color.White)
                    }

                    is AsyncImagePainter.State.Error -> {
                        MediaError(onRetry = { md.painter.restart() })
                    }

                    is AsyncImagePainter.State.Empty -> {}
                }

                is MediaData.Video -> VideoViewer(md.url)
            }
        }

        AnimatedVisibility(visible = !immersive, enter = fadeIn(), exit = fadeOut()) {
            if (mediaItems.size > 1) {
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
                        "Item ${pagerState.currentPage + 1}/${mediaItems.size}",
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(CARD_PADDING),
                    )
                }
            }
        }
    }
}

private sealed class MediaData {
    data class Image(
        val painter: AsyncImagePainter,
        val state: androidx.compose.runtime.State<Any>,
        val zoomableState: ZoomableState,
    ) : MediaData()

    data class Video(
        val url: String,
    ) : MediaData()
}

@Preview
@Composable
private fun PreviewGalleryViewer() {
    val images = listOf(
        MediaItem(
            mediaType = MediaType.Image,
            url = "https://i.redd.it/yrjzzr5st6ud1.jpeg",
            description = null,
            width = null,
            height = null,
        )
    ).toImmutableList()
    GalleryViewer(mediaItems = images)
}