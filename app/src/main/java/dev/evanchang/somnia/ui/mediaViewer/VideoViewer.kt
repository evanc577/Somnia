package dev.evanchang.somnia.ui.mediaViewer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoViewer(mediaItem: MediaItem) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Save current playback state in case screen rotates and configuration is lost
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var currentPosition by rememberSaveable { mutableStateOf(0L) }
    var immersive by rememberSaveable { mutableStateOf(true) }

    // Create player
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
            setControllerVisibilityListener(object : PlayerView.ControllerVisibilityListener {
                // Set immersive variable depending on controller visibility
                override fun onVisibilityChanged(visibility: Int) {
                    if (isControllerFullyVisible) {
                        immersive = false
                    } else {
                        immersive = true
                    }
                }
            })
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    })

    // Toggle immersive mode
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

    // Clean up player
    DisposableEffect(lifecycleOwner) {
        onDispose {
            isPlaying = exoPlayer.isPlaying
            currentPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
    }
}