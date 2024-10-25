package dev.evanchang.somnia.ui.mediaViewer

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoSettings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun VideoViewer(videoUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Save current playback state in case screen rotates and configuration is lost
    var savedIsPlaying by rememberSaveable { mutableStateOf(true) }
    var savedCurrentPosition by rememberSaveable { mutableLongStateOf(0L) }
    var immersive by rememberSaveable { mutableStateOf(false) }

    val mediaItem = remember { MediaItem.fromUri(videoUrl) }

    // Cache
    MediaCache.initialize(context)

    // Create player
    val exoPlayer = remember {
        ExoPlayer.Builder(context).setMediaSourceFactory(
            DefaultMediaSourceFactory(context).setDataSourceFactory(MediaCache.getCacheDataStoreFactory())
        ).setSeekForwardIncrementMs(5000).setSeekBackIncrementMs(5000).build().apply {
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = savedIsPlaying
            setSeekParameters(SeekParameters.EXACT)
            trackSelectionParameters =
                this.trackSelectionParameters.buildUpon().setForceHighestSupportedBitrate(true)
                    .build()
            seekTo(savedCurrentPosition)
            prepare()
        }
    }

    // Continuously update progress bar while playing
    var isPlaying by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var tracks: ImmutableList<Tracks.Group> by remember { mutableStateOf(listOf<Tracks.Group>().toImmutableList()) }
    exoPlayer.addListener(object : Player.Listener {
        override fun onIsPlayingChanged(p: Boolean) {
            isPlaying = p
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            isReady = playbackState == Player.STATE_READY
        }

        override fun onTracksChanged(_tracks: Tracks) {
            tracks = _tracks.groups.toList().toImmutableList()
        }
    })
    LaunchedEffect(Unit) {
        while (true) {
            duration = exoPlayer.duration
            currentPosition = exoPlayer.currentPosition
            bufferedPosition = exoPlayer.bufferedPosition
            delay(100.milliseconds)
        }
    }

    // Toggle immersive mode
    val view = LocalView.current
    LaunchedEffect(immersive) {
        if (immersive) {
            view.windowInsetsController?.hide(
                WindowInsets.Type.systemBars()
            )
        } else {
            view.windowInsetsController?.show(
                WindowInsets.Type.systemBars()
            )
        }
    }

    // Clean up player
    DisposableEffect(lifecycleOwner) {
        onDispose {
            savedIsPlaying = exoPlayer.isPlaying
            savedCurrentPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
    }

    // Viewer dimensions
    var viewerWidth by remember { mutableFloatStateOf(0f) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                viewerWidth = it.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    // Toggle immersive mode on single tap
                    immersive = !immersive
                }, onDoubleTap = { offset ->
                    // Fast-forward/rewind on double tap
                    if (offset.x < viewerWidth / 2) {
                        exoPlayer.seekBack()
                    } else {
                        exoPlayer.seekForward()
                    }
                })
            },
    ) {
        val (controls, video) = createRefs()

        AndroidView(modifier = Modifier
            .fillMaxSize()
            .constrainAs(video) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                setShowNextButton(false)
                setShowPreviousButton(false)
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        })

        AnimatedVisibility(
            visible = !immersive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.constrainAs(controls) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
        ) {
            VideoViewerControls(
                isPlaying = isPlaying,
                isReady = isReady,
                currentPosition = currentPosition,
                bufferedPosition = bufferedPosition,
                totalDuration = duration,
                onPause = { exoPlayer.pause() },
                onPlay = { exoPlayer.play() },
                onSeekTo = { position ->
                    exoPlayer.seekTo(position)
                },
                onGetTracks = { tracks },
                onGetSpeed = { exoPlayer.playbackParameters.speed },
                onSetSpeed = { exoPlayer.setPlaybackSpeed(it) },
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoViewerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isReady: Boolean,
    currentPosition: Long,
    bufferedPosition: Long,
    totalDuration: Long,
    onPause: () -> Unit,
    onPlay: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onGetTracks: () -> ImmutableList<Tracks.Group>,
    onGetSpeed: () -> Float,
    onSetSpeed: (Float) -> Unit,
) {
    val density = LocalDensity.current

    var navBarHeight by remember { mutableStateOf(0.dp) }
    val currentNavBarHeight = with(density) {
        androidx.compose.foundation.layout.WindowInsets.safeDrawing.getBottom(density).toDp()
    }
    LaunchedEffect(currentNavBarHeight) {
        navBarHeight = currentNavBarHeight.coerceAtLeast(navBarHeight)
    }

    // Progress bar look
    var barWidth by remember { mutableIntStateOf(0) }
    val barHeight = 4.dp
    val circleSize = 16.dp
    val clipShape = RoundedCornerShape(16.dp)
    val primaryColor = Color.White
    val bufferColor = Color.Gray
    val backgroundColor = Color.DarkGray

    if (totalDuration <= 0) {
        return
    }

    // Calculated progress
    val bufferProgress = bufferedPosition.toFloat() / totalDuration
    var currentPositionString by remember { mutableStateOf(currentPosition.toTimeString()) }
    val totalDurationString by remember { mutableStateOf(totalDuration.toTimeString()) }

    // Progress bar dragging
    var dragState by remember { mutableStateOf(DragState.NOT_DRAGGING) }
    var playbackOffsetPx by remember {
        mutableIntStateOf((currentPosition.toFloat() / totalDuration * barWidth).roundToInt())
    }
    LaunchedEffect(currentPosition, totalDuration, dragState, isReady) {
        when (dragState) {
            DragState.NOT_DRAGGING -> if (isReady) {
                val percentage = (currentPosition.toFloat() / totalDuration)
                playbackOffsetPx = (percentage * barWidth).roundToInt()
                currentPositionString = (percentage * totalDuration).toLong().toTimeString()
            }

            DragState.DRAGGING -> {
                val percentage = playbackOffsetPx.toFloat() / barWidth
                currentPositionString = (percentage * totalDuration).toLong().toTimeString()
            }

            DragState.DRAG_FINISHED -> {
                onSeekTo((playbackOffsetPx.toFloat() / barWidth * totalDuration).roundToLong())
                dragState = DragState.NOT_DRAGGING
            }
        }
    }

    var showOptions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Play/pause
            if (isPlaying) {
                IconButton(onClick = onPause) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        tint = Color.White,
                        contentDescription = "",
                    )
                }
            } else {
                IconButton(onClick = onPlay) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        tint = Color.White,
                        contentDescription = "",
                    )
                }
            }

            VideoTimeText(text = currentPositionString)

            // Progress bar
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .onGloballyPositioned {
                        with(density) {
                            barWidth = it.size.width - circleSize
                                .toPx()
                                .roundToInt()
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { offset ->
                            playbackOffsetPx = offset.x.roundToInt()
                            dragState = DragState.DRAGGING
                            if (tryAwaitRelease()) {
                                dragState = DragState.DRAG_FINISHED
                            }
                        })
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                playbackOffsetPx = offset.x.roundToInt()
                                dragState = DragState.DRAGGING
                            },
                            onDragEnd = { dragState = DragState.DRAG_FINISHED },
                            onHorizontalDrag = { _, delta ->
                                playbackOffsetPx = (playbackOffsetPx + delta.roundToInt()).coerceIn(
                                    0, barWidth
                                )
                            },
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = (circleSize - barHeight) / 2)
                        .padding(horizontal = circleSize / 2)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(clipShape)
                            .background(backgroundColor)
                            .height(barHeight)
                            .fillMaxWidth(),
                    )

                    Box(
                        modifier = Modifier
                            .clip(clipShape)
                            .background(bufferColor)
                            .height(barHeight)
                            .fillMaxWidth(bufferProgress),
                    )

                    Box(
                        modifier = Modifier
                            .clip(clipShape)
                            .background(primaryColor)
                            .height(barHeight)
                            .fillMaxWidth(playbackOffsetPx.toFloat() / barWidth),
                    )
                }

                Box(modifier = Modifier
                    .size(circleSize)
                    .offset {
                        IntOffset(x = playbackOffsetPx, y = 0)
                    }
                    .drawWithCache {
                        onDrawBehind { drawCircle(color = primaryColor) }
                    })
            }

            // Total duration
            VideoTimeText(text = totalDurationString)

            // Video options
            IconButton(onClick = { showOptions = true }) {
                Icon(
                    imageVector = Icons.Default.VideoSettings,
                    tint = Color.White,
                    contentDescription = "video settings",
                )
            }
        }

        Spacer(modifier = Modifier.height(navBarHeight))
    }

    if (showOptions) {
        VideoOptions(
            onDismissRequest = { showOptions = false },
            onGetSpeed = onGetSpeed,
            onSetSpeed = onSetSpeed,
        )
    }
}

@Composable
private fun VideoOptions(
    onDismissRequest: () -> Unit, onGetSpeed: () -> Float, onSetSpeed: (Float) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(onGetSpeed().speedToSlider()) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Speed: ${sliderPosition.sliderToSpeed()}×", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    steps = 6,
                    valueRange = 0f..7f,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = "Cancel")
                    }
                    TextButton(onClick = {
                        onSetSpeed(sliderPosition.sliderToSpeed())
                        onDismissRequest()
                    }) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}

private fun Float.speedToSlider(): Float {
    val epsilon = 0.0001f
    return if (this - 0.25f < epsilon) {
        0f
    } else if (this - 0.5f < epsilon) {
        1f
    } else if (this - 0.75f < epsilon) {
        2f
    } else if (this - 1f < epsilon) {
        3f
    } else if (this - 1.25f < epsilon) {
        4f
    } else if (this - 1.5f < epsilon) {
        5f
    } else if (this - 1.75f < epsilon) {
        6f
    } else {
        7f
    }
}

private fun Float.sliderToSpeed(): Float {
    val epsilon = 0.0001f
    return if (this - 0f < epsilon) {
        0.25f
    } else if (this - 1f < epsilon) {
        0.5f
    } else if (this - 2f < epsilon) {
        0.75f
    } else if (this - 3f < epsilon) {
        1f
    } else if (this - 4f < epsilon) {
        1.25f
    } else if (this - 5f < epsilon) {
        1.5f
    } else if (this - 6f < epsilon) {
        1.75f
    } else {
        2f
    }
}

@Preview(heightDp = 400, widthDp = 400)
@Composable
private fun VideoOptionsPreview() {
    VideoOptions(onDismissRequest = {}, onGetSpeed = { 1f }, onSetSpeed = {})
}

enum class DragState {
    NOT_DRAGGING, DRAGGING, DRAG_FINISHED,
}

@SuppressLint("DefaultLocale")
private fun Long.toTimeString(): String {
    if (this < 0) {
        return "??:??"
    }
    val seconds = (this / 1000) % 60
    val minutes = this / 1000 / 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun VideoTimeText(text: String) {
    Text(
        text = text,
        color = Color.White,
        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Preview(widthDp = 400)
@Composable
private fun VideoViewerControlsPreview() {
    VideoViewerControls(
        isPlaying = false,
        isReady = true,
        currentPosition = 100_000,
        bufferedPosition = 200_000,
        totalDuration = 300_000,
        onPause = {},
        onPlay = {},
        onSeekTo = {},
        onGetTracks = { listOf<Tracks.Group>().toImmutableList() },
        onGetSpeed = { 1f },
        onSetSpeed = {},
    )
}

@UnstableApi
object MediaCache {
    private const val CACHE_SIZE: Long = 100 * 1024 * 1024 // 100 MB
    private lateinit var cache: SimpleCache
    private lateinit var databaseProvider: DatabaseProvider
    private lateinit var cacheDataSourceFactory: CacheDataSource.Factory

    fun initialize(context: Context) {
        if (!::databaseProvider.isInitialized) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        if (!::cache.isInitialized) {
            val cacheDir = File(context.cacheDir, "media3_cache")
            cache =
                SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(CACHE_SIZE), databaseProvider)
        }
        if (!::cacheDataSourceFactory.isInitialized) {
            cacheDataSourceFactory = CacheDataSource.Factory().setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
        }
    }

    fun getCacheDataStoreFactory(): CacheDataSource.Factory {
        if (!::cacheDataSourceFactory.isInitialized) {
            throw IllegalStateException("CacheDataStoreFactory not initialized. Call initialize(context) first.")
        }
        return cacheDataSourceFactory
    }
}