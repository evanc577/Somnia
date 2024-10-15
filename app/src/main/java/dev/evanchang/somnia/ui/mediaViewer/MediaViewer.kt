package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.evanchang.somnia.data.Submission

@Composable
fun MediaViewer(
    viewModel: MediaViewerViewModel = viewModel(),
    submission: Submission,
    onClose: () -> Unit,
) {
    val images = submission.images()
    if (images == null) {
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
            GalleryViewer(images)
        }
    }
}