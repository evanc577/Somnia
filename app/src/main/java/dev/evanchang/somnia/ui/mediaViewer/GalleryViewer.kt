package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.request.ImageRequest
import dev.evanchang.somnia.ui.util.ImageLoading
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryViewer(
    images: List<String>,
) {
    val pagerState = rememberPagerState { images.size }
    val loaded = remember { images.map({ _ -> false }).toMutableStateList() }

    HorizontalPager(state = pagerState, beyondViewportPageCount = 1) { imageIdx ->
        val imageRequest =
            ImageRequest.Builder(LocalContext.current).data(images[imageIdx])
                .listener(
                    onSuccess = { _, _ -> loaded[imageIdx] = true },
                )
                .crossfade(true)
                .build()
        if (!loaded[imageIdx]) {
            ImageLoading(color = Color.White)
        }
        ZoomableAsyncImage(
            model = imageRequest,
            contentDescription = "Submission image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview
@Composable
private fun PreviewGalleryViewer() {
    val images = arrayListOf("https://i.redd.it/yrjzzr5st6ud1.jpeg")
    GalleryViewer(images = images)
}