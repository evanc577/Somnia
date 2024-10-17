package dev.evanchang.somnia.Ui.MediaViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.evanchang.somnia.Data.Media
import dev.evanchang.somnia.Data.Submission

@Composable
fun MediaViewer(
    viewModel: MediaViewerViewModel = viewModel(),
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