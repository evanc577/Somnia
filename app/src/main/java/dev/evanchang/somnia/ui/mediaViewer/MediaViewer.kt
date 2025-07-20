package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.api.media.MediaItem
import dev.evanchang.somnia.ui.util.MediaError
import dev.evanchang.somnia.ui.util.MediaLoading
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaViewer(
    media: Media,
    onClose: () -> Unit,
) {
    LocalDensity.current
    LocalConfiguration.current

    // Load media
    var mediaItems: ApiResult<ImmutableList<MediaItem>>? by remember { mutableStateOf(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(loading) {
        if (loading) {
            mediaItems = media.fetchData()
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color.Black)
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (loading || mediaItems == null) {
                MediaLoading(color = Color.White)
            } else {
                when (val m = mediaItems!!) {
                    is ApiResult.Ok -> GalleryViewer(m.value)
                    is ApiResult.Err -> MediaError(onRetry = { loading = true })
                }
            }
        }
    }
}