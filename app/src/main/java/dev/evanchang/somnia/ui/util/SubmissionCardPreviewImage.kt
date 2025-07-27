package dev.evanchang.somnia.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.navigation.LocalNavigation
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.UiConstants
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SubmissionCardPreviewImage(
    submission: Submission,
    compact: Boolean,
    modifier: Modifier = Modifier.Companion,
) {
    val nav = LocalNavigation.current
    val previewImage = remember { submission.previewImage() } ?: return
    val previewImageUrl = remember { previewImage.escapedUrl() }

    val (previewImageWidth, previewImageHeight) = if (compact) {
        Pair(1, 1)
    } else {
        Pair(previewImage.width, previewImage.height)
    }

    val context = LocalPlatformContext.current
    val painter = rememberAsyncImagePainter(model = remember {
        ImageRequest.Builder(context).data(previewImageUrl).build()
    })
    val state = painter.state.collectAsStateWithLifecycle(context)

    val media = remember { submission.media() }
    val previewIconType = remember {
        if (media != null) {
            PreviewIconType.Media(media)
        } else if (submission.isSelf) {
            null
        } else {
            PreviewIconType.Link()
        }
    }
    val previewIconAlignment = remember {
        if (compact) {
            Alignment.Companion.Center
        } else {
            Alignment.Companion.TopStart
        }
    }

    Card(
        onClick = {
            val media = submission.media()
            if (media != null) {
                nav.onNavigate(Nav.MediaViewer(media))
            }
        },
        shape = RoundedCornerShape(UiConstants.ROUNDED_CORNER_RADIUS),
        modifier = modifier
            .thenIf(compact) {
                Modifier.Companion.size(80.dp)
            }
            .fillMaxWidth()
            .aspectRatio(previewImageWidth.toFloat() / previewImageHeight),
    ) {
        when (state.value) {
            is AsyncImagePainter.State.Success -> {
                Box {
                    Image(
                        painter = painter,
                        contentDescription = "submission image",
                        contentScale = if (compact) {
                            ContentScale.Companion.Crop
                        } else {
                            ContentScale.Companion.FillWidth
                        },
                        modifier = Modifier.Companion
                            .fillMaxSize()
                            .aspectRatio(previewImageWidth.toFloat() / previewImageHeight.toFloat()),
                    )
                    if (previewIconType != null) {
                        PreviewIcon(
                            type = previewIconType,
                            compact = compact,
                            modifier = Modifier.Companion.padding(UiConstants.CARD_PADDING),
                            contentAlignment = previewIconAlignment,
                        )
                    }
                }
            }

            is AsyncImagePainter.State.Error -> {
                PreviewImageError(compact = compact, onRetry = { painter.restart() })
            }

            else -> {
                PreviewImageLoading()
            }
        }
    }
}

private sealed class PreviewIconType {
    data class Media(val media: dev.evanchang.somnia.api.media.Media) : PreviewIconType()
    class Link : PreviewIconType()

    fun data(): PreviewIconData {
        val linkIcon = Icons.Outlined.Link
        val galleryIcon = Icons.Outlined.PhotoLibrary
        val videoIcon = Icons.Outlined.OndemandVideo

        return when (this) {
            is PreviewIconType.Link -> PreviewIconData(linkIcon, null)
            is PreviewIconType.Media -> when (this.media) {
                is dev.evanchang.somnia.api.media.Media.ImgurAlbum -> PreviewIconData(
                    galleryIcon, "Imgur"
                )

                is dev.evanchang.somnia.api.media.Media.ImgurMedia -> PreviewIconData(
                    galleryIcon, "Imgur"
                )

                is dev.evanchang.somnia.api.media.Media.RedditGallery -> PreviewIconData(
                    galleryIcon, media.images.size.toString()
                )

                is dev.evanchang.somnia.api.media.Media.RedditVideo -> PreviewIconData(
                    videoIcon, null
                )

                is dev.evanchang.somnia.api.media.Media.Redgifs -> PreviewIconData(
                    videoIcon, "Redgifs"
                )

                is dev.evanchang.somnia.api.media.Media.Streamable -> PreviewIconData(
                    videoIcon, "Streamable"
                )
            }
        }
    }
}

@Composable
private fun PreviewImageLoading() {
    Box(
        modifier = Modifier.Companion.background(color = MaterialTheme.colorScheme.surfaceDim)
    ) {
        MediaLoading()
    }
}

@Composable
private fun PreviewImageError(compact: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.Companion
            .background(color = MaterialTheme.colorScheme.errorContainer)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        if (compact) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.error
            )
        } else {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.Companion.width(UiConstants.SPACER_SIZE))
                Text(
                    text = "Image failed to load",
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(modifier = Modifier.Companion.height(UiConstants.SPACER_SIZE))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(text = "Retry")
            }
        }
    }
}

private class PreviewIconData(
    val icon: ImageVector,
    val text: String?
)

@Composable
private fun PreviewIcon(
    type: PreviewIconType,
    compact: Boolean,
    modifier: Modifier = Modifier.Companion,
    contentAlignment: Alignment = Alignment.Companion.TopStart
) {

    if (type is PreviewIconType.Media && type.media is Media.RedditGallery && type.media.images.size <= 1) {
        return
    }
    val data = remember { type.data() }

    Box(contentAlignment = contentAlignment, modifier = Modifier.Companion.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(UiConstants.ROUNDED_CORNER_RADIUS),
            modifier = modifier,
        ) {
            Row(modifier = Modifier.Companion.padding(8.dp)) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = ""
                )
                if (!compact && data.text != null) {
                    Spacer(modifier = Modifier.Companion.size(UiConstants.SPACER_SIZE))
                    Text(text = data.text)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewIconPreview() {
    Column {
        PreviewIcon(
            type = PreviewIconType.Media(Media.RedditGallery(listOf<String>().toImmutableList())),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Media(Media.RedditVideo("")),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Media(Media.ImgurAlbum("")),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Media(Media.ImgurMedia("")),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Media(Media.Redgifs("")),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Media(Media.Streamable("")),
            compact = false,
        )
        PreviewIcon(
            type = PreviewIconType.Link(),
            compact = false,
        )
    }
}

@Preview(widthDp = 400, heightDp = 300)
@Composable
private fun PreviewImageErrorPreview() {
    PreviewImageError(compact = false, onRetry = {})
}