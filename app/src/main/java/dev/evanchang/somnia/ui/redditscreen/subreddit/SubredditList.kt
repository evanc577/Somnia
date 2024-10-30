package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import dev.evanchang.somnia.ui.util.SubmissionCard
import dev.evanchang.somnia.ui.util.SubmissionCardMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditList(
    subredditViewModel: SubredditViewModel,
    listState: LazyListState,
    topPadding: Dp,
    onClickSubreddit: (String) -> Unit,
    onClickSubmission: (Submission) -> Unit,
) {
    val lazySubmissionItems: LazyPagingItems<Submission> =
        subredditViewModel.submissions.collectAsLazyPagingItems()
    val isRefreshing by subredditViewModel.isRefreshing.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = remember {
        {
            subredditViewModel.updateIsRefreshing(true)
            coroutineScope.launch {
                lazySubmissionItems.refresh()
            }
        }
    }

    LaunchedEffect(lazySubmissionItems.loadState.refresh, isRefreshing) {
        if (isRefreshing && lazySubmissionItems.loadState.refresh != LoadState.Loading) {
            subredditViewModel.updateIsRefreshing(false)
            listState.scrollToItem(0)
        }
    }

    // Media viewer
    val mediaViewerState = subredditViewModel.mediaViewerState.collectAsStateWithLifecycle()
    when (val s = mediaViewerState.value) {
        is MediaViewerState.Showing -> {
            MediaViewer(
                submission = s.submission,
                onClose = {
                    subredditViewModel.setMediaViewerState(MediaViewerState.NotShowing)
                },
            )
        }

        else -> {}
    }

    // Submissions list
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            items(count = lazySubmissionItems.itemCount,
                key = { index -> lazySubmissionItems[index]!!.name }) { index ->
                val submission = lazySubmissionItems[index]
                if (submission != null) {
                    SubmissionCard(
                        submission = submission,
                        mode = SubmissionCardMode.PREVIEW_FULL,
                        setShowMediaViewerState = {
                            subredditViewModel.setMediaViewerState(it)
                        },
                        onClickSubreddit = onClickSubreddit,
                        onClickSubmission = onClickSubmission,
                    )
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