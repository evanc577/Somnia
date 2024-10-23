package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.evanchang.somnia.ui.util.ImageLoading
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun GalleryViewer(
    images: PersistentList<String>,
) {
    val context = LocalPlatformContext.current
    val pagerState = rememberPagerState { images.size }
    val painters = images.map { image ->
        rememberAsyncImagePainter(model = remember {
            ImageRequest.Builder(context).data(image).crossfade(true).build()
        })
    }.toImmutableList()
    val states = painters.map { painter ->
        painter.state.collectAsStateWithLifecycle(context)
    }.toImmutableList()

    HorizontalPager(state = pagerState, beyondViewportPageCount = 1) { idx ->
        when (states[idx].value) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painters[idx],
                    contentDescription = "gallery image $idx",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
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
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Image failed to load",
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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
    val images = persistentListOf("https://i.redd.it/yrjzzr5st6ud1.jpeg")
    GalleryViewer(images = images)
}