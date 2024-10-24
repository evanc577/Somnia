package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.evanchang.somnia.data.Media
import dev.evanchang.somnia.data.Submission

@Composable
fun MediaViewer(
    submission: Submission,
    onClose: () -> Unit,
) {
    val media = remember { submission.media() }
    if (media == null) {
        return
    }

    DialogFullScreen(
        onDismissRequest = onClose,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color.Black,
                )
        ) {
            when (media) {
                is Media.Images -> GalleryViewer(media.images)
                is Media.RedditVideo -> VideoViewer(media.video)
            }
        }
    }
}