package dev.evanchang.somnia.ui.util

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.ModeComment
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import dev.evanchang.somnia.api.media.Media
import dev.evanchang.somnia.data.PreviewImage
import dev.evanchang.somnia.data.PreviewImages
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionPreview
import dev.evanchang.somnia.ui.UiConstants.BODY_TEXT_PADDING
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.ROUNDED_CORNER_RADIUS
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import dev.evanchang.somnia.ui.theme.SomniaTheme
import eu.wewox.textflow.material3.TextFlow
import eu.wewox.textflow.material3.TextFlowObstacleAlignment
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

enum class SubmissionCardMode {
    PREVIEW_FULL, DETAILS,
}

@Composable
fun SubmissionCard(
    submission: Submission,
    mode: SubmissionCardMode,
    setShowMediaViewerState: (MediaViewerState) -> Unit,
    onClickSubreddit: (String) -> Unit,
    onClickSubmission: ((Submission) -> Unit)? = null,
) {
    LocalContext.current
    val media = remember { submission.media() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
        modifier = Modifier.thenIf(onClickSubmission != null) {
            Modifier.clickable { onClickSubmission!!(submission) }
        },
    ) {
        Column(modifier = Modifier.padding(all = CARD_PADDING)) {
            SubmissionCardHeader(
                submission = submission,
                onClickSubreddit = onClickSubreddit,
            )
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            if (mode == SubmissionCardMode.PREVIEW_FULL && media != null) {
                Text(
                    text = submission.escapedTitle(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(SPACER_SIZE))
                PreviewImage(
                    submission = submission,
                    compact = false,
                    setShowMediaViewerState = setShowMediaViewerState,
                )
            } else {
                TextFlow(
                    text = submission.escapedTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    obstacleAlignment = TextFlowObstacleAlignment.TopEnd,
                ) {
                    Box(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                        PreviewImage(
                            submission = submission,
                            compact = true,
                            setShowMediaViewerState = setShowMediaViewerState,
                        )
                    }
                }
            }
            if (submission.selftext.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SPACER_SIZE))
                Card(
                    colors = CardDefaults.cardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
                ) {
                    when (mode) {
                        SubmissionCardMode.PREVIEW_FULL -> SomniaMarkdown(
                            content = submission.selftext,
                            isPreview = true,
                            modifier = Modifier
                                .padding(BODY_TEXT_PADDING)
                                .fillMaxWidth(),
                        )

                        SubmissionCardMode.DETAILS -> SomniaMarkdown(
                            content = submission.selftext,
                            isPreview = false,
                            modifier = Modifier
                                .padding(BODY_TEXT_PADDING)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            SubmissionCardFooter(submission = submission)
        }
    }
}

@Composable
private fun SubmissionCardHeader(submission: Submission, onClickSubreddit: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    ) {
                        append("r/")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(submission.subreddit)
                    }
                },
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable {
                    onClickSubreddit(submission.subreddit)
                },
            )
            Spacer(modifier = Modifier.height(SPACER_SIZE))
            Text(
                text = "u/${submission.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.weight(1.0f))
        ElapsedTime(submission = submission, modifier = Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
private fun ElapsedTime(submission: Submission, modifier: Modifier = Modifier) {
    Text(
        text = submission.elapsedTimeString(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun SubmissionCardFooter(submission: Submission) {
    Row(
        horizontalArrangement = Arrangement.End, modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        CommentsButton(submission = submission)
        Spacer(modifier = Modifier.width(SPACER_SIZE))
        ScoreButton(submission = submission)
    }
}

@Composable
private fun ScoreButton(submission: Submission) {
    val context = LocalContext.current
    Card(
        onClick = {
            // TODO: implement voting
            Toast.makeText(context, "TODO: implement voting", Toast.LENGTH_SHORT).show()
        },
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = submission.score.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CommentsButton(submission: Submission) {
    Card(shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ModeComment,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(SPACER_SIZE))
            Text(
                text = submission.numComments.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
            )
        }
    }
}

@Composable
private fun PreviewImage(
    modifier: Modifier = Modifier,
    submission: Submission,
    compact: Boolean,
    setShowMediaViewerState: (MediaViewerState) -> Unit,
) {
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
        } else {
            PreviewIconType.Link()
        }
    }
    val previewIconAlignment = remember {
        if (compact) {
            Alignment.Center
        } else {
            Alignment.TopStart
        }
    }

    Card(
        onClick = {
            setShowMediaViewerState(MediaViewerState.Showing(submission))
        },
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
        modifier = modifier
            .thenIf(compact) {
                Modifier.size(80.dp)
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
                            ContentScale.Crop
                        } else {
                            ContentScale.FillWidth
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(previewImageWidth.toFloat() / previewImageHeight.toFloat()),
                    )
                    PreviewIcon(
                        type = previewIconType,
                        modifier = Modifier.padding(CARD_PADDING),
                        contentAlignment = previewIconAlignment,
                    )
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

@Composable
private fun PreviewImageLoading() {
    Box(
        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceDim)
    ) {
        MediaLoading()
    }
}

@Composable
private fun PreviewImageError(compact: Boolean, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.errorContainer)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (compact) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.error
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(SPACER_SIZE))
                Text(
                    text = "Image failed to load",
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(modifier = Modifier.height(SPACER_SIZE))
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

private sealed class PreviewIconType {
    data class Media(val media: dev.evanchang.somnia.api.media.Media) : PreviewIconType()
    class Link : PreviewIconType()
}

@Composable
private fun PreviewIcon(
    type: PreviewIconType,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart
) {
    val linkIcon = Icons.Outlined.Link
    val galleryIcon = Icons.Outlined.PhotoLibrary
    val videoIcon = Icons.Outlined.OndemandVideo

    if (type is PreviewIconType.Media && type.media is Media.RedditGallery && type.media.images.size <= 1) {
        return
    }

    Box(contentAlignment = contentAlignment, modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
            modifier = modifier,
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                when (type) {
                    is PreviewIconType.Link -> Icon(
                        imageVector = linkIcon,
                        contentDescription = "link",
                    )

                    is PreviewIconType.Media -> {
                        when (type.media) {
                            is Media.ImgurAlbum -> Text(text = "Imgur Gallery")
                            is Media.ImgurMedia -> Text(text = "Imgur")
                            is Media.RedditGallery -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = galleryIcon,
                                    contentDescription = "album",
                                )
                                Spacer(modifier = Modifier.size(SPACER_SIZE))
                                Text(text = type.media.images.size.toString())
                            }

                            is Media.RedditVideo -> Icon(
                                imageVector = videoIcon,
                                contentDescription = "video",
                            )

                            is Media.Redgifs -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = videoIcon,
                                    contentDescription = "video",
                                )
                                Spacer(modifier = Modifier.size(SPACER_SIZE))
                                Text(text = "Redgifs")
                            }

                            is Media.Streamable -> Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = videoIcon,
                                    contentDescription = "video",
                                )
                                Spacer(modifier = Modifier.size(SPACER_SIZE))
                                Text(text = "Streamable")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewIconPreview() {
    Column {
        PreviewIcon(PreviewIconType.Media(Media.RedditGallery(listOf<String>().toImmutableList())))
        PreviewIcon(PreviewIconType.Media(Media.RedditVideo("")))
        PreviewIcon(PreviewIconType.Media(Media.ImgurAlbum("")))
        PreviewIcon(PreviewIconType.Media(Media.ImgurMedia("")))
        PreviewIcon(PreviewIconType.Media(Media.Redgifs("")))
        PreviewIcon(PreviewIconType.Media(Media.Streamable("")))
        PreviewIcon(PreviewIconType.Link())
    }
}

@Preview(widthDp = 400, heightDp = 300)
@Composable
private fun PreviewImageErrorPreview() {
    PreviewImageError(compact = false, onRetry = {})
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubmissionCardPreview() {
    val submission = createFakeSubmission()

    SomniaTheme {
        LazyColumn {
            for (i in 1..3) {
                item {
                    SubmissionCard(
                        submission = submission,
                        mode = SubmissionCardMode.PREVIEW_FULL,
                        setShowMediaViewerState = {},
                        onClickSubreddit = {},
                        onClickSubmission = {},
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubmissionCardDetailsPreview() {
    SomniaTheme {
        SubmissionCard(
            submission = createFakeSubmission(),
            mode = SubmissionCardMode.DETAILS,
            setShowMediaViewerState = {},
            onClickSubreddit = {},
            onClickSubmission = {},
        )
    }
}

@Suppress("SpellCheckingInspection")
private fun createFakeSubmission(): Submission {
    val selftext = """
        # Mattis facilisi venenatis rhoncus; tellus nibh nostra mattis ornare.
        
        Amet sem habitant ac lobortis eleifend laoreet.
        Eleifend vel risus cubilia id auctor cras.
        Pretium vehicula class elementum duis varius arcu neque vivamus cubilia.
        Varius tristique dui sapien ipsum primis aptent maximus accumsan.
        Facilisis fermentum taciti pulvinar eleifend sem dis cras.
        Urna tempor at dignissim ridiculus dolor sed iaculis auctor.
        Cras nec penatibus a augue curabitur inceptos non.
    """.trimIndent()
    return Submission(
        name = "",
        id = "",
        author = "author",
        subreddit = "subreddit",
        title = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
            Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
        """.trimIndent(),
        selftext = selftext,
        postHint = null,
        isGallery = null,
        url = "",
        domain = "",
        preview = SubmissionPreview(
            persistentListOf(
                PreviewImages(
                    source = PreviewImage(
                        url = "https://i.imgur.com/c10Qvha.jpg", 1800, 1200
                    ),
                    resolutions = listOf<PreviewImage>().toImmutableList(),
                )
            )
        ),
        mediaMetadata = null,
        media = null,
        score = 10,
        numComments = 20,
        created = 1700000000f,
        galleryData = null,
        permalink = "",
    )
}