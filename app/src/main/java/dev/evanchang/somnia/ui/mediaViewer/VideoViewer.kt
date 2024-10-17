package dev.evanchang.somnia.ui.mediaViewer

import android.content.Context
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoViewer(mediaItem: MediaItem) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Save current playback state in case screen rotates and configuration is lost
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    var currentPosition by rememberSaveable { mutableStateOf(0L) }
    var immersive by rememberSaveable { mutableStateOf(true) }

    // Cache
    MediaCache.initialize(context)

    // Create player
    val exoPlayer = remember {
        ExoPlayer.Builder(context).setMediaSourceFactory(
            DefaultMediaSourceFactory(context).setDataSourceFactory(MediaCache.getCacheDataStoreFactory())
        ).build().apply {
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
                    immersive = !isControllerFullyVisible
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