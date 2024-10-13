package dev.evanchang.somnia.ui.mediaViewer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.evanchang.somnia.data.PostHint
import dev.evanchang.somnia.data.Submission

@Composable
fun MediaViewer(
    submission: Submission,
    onClose: () -> Unit,
) {
    if (submission.postHint != PostHint.IMAGE) {
        return
    }

    val imageRequest =
        ImageRequest.Builder(LocalContext.current).data(submission.url).crossfade(true)
            .build()
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
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = "Submission image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}