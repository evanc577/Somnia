package dev.evanchang.somnia.ui.mediaViewer

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoViewer(mediaItem: MediaItem) {
    // Save current playback state in case screen rotates and configuration is lost
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var currentPosition by rememberSaveable { mutableStateOf(0L) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = isPlaying
            seekTo(currentPosition)
            prepare()
        }
    }

    AndroidView(modifier = Modifier.fillMaxSize(), factory = {
        PlayerView(context).apply {
            player = exoPlayer
            useController = true
            setShowNextButton(false)
            setShowPreviousButton(false)
            setShowFastForwardButton(false)
            setShowRewindButton(false)
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    })
    DisposableEffect(lifecycleOwner) {
        onDispose {
            isPlaying = exoPlayer.isPlaying
            currentPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
    }
}