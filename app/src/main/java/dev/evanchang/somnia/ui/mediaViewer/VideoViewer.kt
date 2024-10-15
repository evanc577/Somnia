package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoViewer(mediaItem: MediaItem) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(exoPlayer, mediaItem) {
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        exoPlayer.prepare()
    }

    val defaultPlayerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = true
        }
    }

    AndroidView(
        factory = {
            defaultPlayerView.apply {}
        }, modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(lifecycleOwner) {
        onDispose {
            exoPlayer.release()
        }
    }
}