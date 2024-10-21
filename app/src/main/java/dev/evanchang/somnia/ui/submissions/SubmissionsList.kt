package dev.evanchang.somnia.ui.submissions

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import dev.evanchang.somnia.data.PreviewImage
import dev.evanchang.somnia.data.PreviewImages
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionPreview
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.theme.SomniaTheme
import dev.evanchang.somnia.ui.util.ImageLoading
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsList(
    submissionsListViewModel: SubmissionsListViewModel,
    listState: LazyStaggeredGridState,
    topPadding: Dp,
) {
    val lazySubmissionItems: LazyPagingItems<Submission> =
        submissionsListViewModel.submissions.collectAsLazyPagingItems()
    val isRefreshing by submissionsListViewModel.isRefreshing.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        submissionsListViewModel.updateIsRefreshing(true)
        coroutineScope.launch {
            lazySubmissionItems.refresh()
        }
    }

    LaunchedEffect(lazySubmissionItems.loadState.refresh, isRefreshing) {
        if (isRefreshing && lazySubmissionItems.loadState.refresh != LoadState.Loading) {
            submissionsListViewModel.updateIsRefreshing(false)
            listState.scrollToItem(0)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                color = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = Modifier.padding(top = topPadding)
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(400.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .fillMaxSize(),
            state = listState
        ) {
            items(count = lazySubmissionItems.itemCount,
                key = { index -> lazySubmissionItems[index]!!.id }) { index ->
                val submission = lazySubmissionItems[index]
                if (submission != null) {
                    SubmissionCard(submission = submission)
                }
            }

            when (val s = lazySubmissionItems.loadState.append) {
                is LoadState.Loading -> item { LinearProgressIndicator() }
                is LoadState.Error -> item {
                    ErrorCard(
                        lazySubmissionItems = lazySubmissionItems,
                        message = s.error.message,
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun ErrorCard(
    lazySubmissionItems: LazyPagingItems<Submission>, message: String?
) {
    Card(
        onClick = {
            lazySubmissionItems.retry()
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.padding(4.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Column {
                Text(
                    text = "Could not fetch posts, tap to retry",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$message",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmissionCard(submission: Submission) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ), onClick = {}, modifier = Modifier.padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            SubmissionCardHeader(submission = submission)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = submission.escapedTitle(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            SubmissionCardPreview(submission = submission)
            Spacer(modifier = Modifier.height(8.dp))
            SubmissionCardFooter(submission = submission)
        }
    }
}

@Composable
private fun SubmissionCardHeader(submission: Submission) {
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
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    Row {
        ScoreButton(submission = submission)
        Spacer(modifier = Modifier.width(8.dp))
        CommentsButton(submission = submission)
    }
}

@Composable
private fun ScoreButton(submission: Submission) {
    Card(onClick = {}) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            Row {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = submission.score.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun CommentsButton(submission: Submission) {
    Card(onClick = {}) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            Row {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ModeComment,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = submission.numComments.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }


        }
    }
}

@Composable
private fun SubmissionCardPreview(submission: Submission) {
    val previewImage = submission.previewImage() ?: return

    val imageRequest =
        ImageRequest.Builder(LocalContext.current).data(previewImage.escapedUrl()).crossfade(true)
            .build()

    var showMediaViewer by rememberSaveable { mutableStateOf(false) }
    if (showMediaViewer) {
        MediaViewer(submission = submission, onClose = { showMediaViewer = false })
    }

    Card(onClick = { showMediaViewer = true }) {
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = "Submission image",
            contentScale = ContentScale.FillWidth,
            loading = { PreviewLoading(width = previewImage.width, height = previewImage.height) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PreviewLoading(width: Int, height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(width.toFloat() / height)
            .background(color = MaterialTheme.colorScheme.surfaceDim)
    ) {
        ImageLoading()
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewPostCard() {
    val submission = Submission(
        id = "",
        author = "author",
        subreddit = "subreddit",
        title = "Post title",
        postHint = null,
        isGallery = null,
        url = "",
        preview = SubmissionPreview(
            listOf(
                PreviewImages(
                    source = PreviewImage(
                        url = "https://i.imgur.com/c10Qvha.jpg", 1800, 1200
                    )
                )
            )
        ),
        mediaMetadata = null,
        media = null,
        score = 10,
        numComments = 20,
        created = 1700000000f,
    )

    SomniaTheme {
        Column {
            for (i in 1..10) {
                SubmissionCard(submission = submission)
            }
        }
    }
}
