package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.evanchang.somnia.api.isWaitForDataStore
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.CARD_SPACING
import dev.evanchang.somnia.ui.UiConstants.ROUNDED_CORNER_RADIUS
import dev.evanchang.somnia.ui.UiConstants.SPACER_SIZE
import dev.evanchang.somnia.ui.util.SubmissionCard
import dev.evanchang.somnia.ui.util.SubmissionCardMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditList(
    vm: SubredditViewModel,
    listState: LazyListState,
    topPadding: Dp,
    bottomPadding: Dp,
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
) {
    val lazySubmissionItems: LazyPagingItems<Submission> =
        vm.submissions.collectAsLazyPagingItems()
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = remember {
        {
            vm.setIsRefreshing(true)
            coroutineScope.launch {
                lazySubmissionItems.refresh()
            }
        }
    }

    LaunchedEffect(
        lazySubmissionItems.loadState,
        isRefreshing,
    ) {
        if (lazySubmissionItems.isWaitForDataStore()) {
            return@LaunchedEffect
        }
        if (isRefreshing && lazySubmissionItems.loadState.refresh != LoadState.Loading) {
            vm.setIsRefreshing(false)
            listState.scrollToItem(0)
        }
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
            verticalArrangement = Arrangement.spacedBy(CARD_SPACING)
        ) {
            // Show error card on refresh error
            when (val s = lazySubmissionItems.loadState.refresh) {
                is LoadState.Error -> if (!lazySubmissionItems.isWaitForDataStore()) {
                    item {
                        ErrorCard(
                            onRetry = onRefresh,
                            message = s.error.message,
                        )
                    }
                }

                else -> {}
            }

            // List of submissions
            items(
                count = lazySubmissionItems.itemCount,
                key = { index -> lazySubmissionItems[index]!!.name },
            ) { index ->
                val submission = lazySubmissionItems[index]
                if (submission != null) {
                    SubmissionCard(
                        submission = submission,
                        mode = SubmissionCardMode.PREVIEW_FULL,
                        onBack = onBack,
                        onNavigate = onNavigate,
                    )
                }
            }

            // Show message if paging has finished
            if (lazySubmissionItems.loadState.append.endOfPaginationReached) {
                item { EndOfPaging() }
            }

            // Show error card on append error
            when (val s = lazySubmissionItems.loadState.append) {
                is LoadState.Loading -> item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                is LoadState.Error -> item {
                    ErrorCard(
                        onRetry = { lazySubmissionItems.retry() },
                        message = s.error.message,
                    )
                }

                else -> Unit
            }

            // Don't draw under nav bar
            item {
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }
    }
}

@Composable
private fun ErrorCard(
    onRetry: () -> Unit, message: String?
) {
    Card(
        onClick = onRetry,
        shape = RoundedCornerShape(ROUNDED_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
    ) {
        Box(modifier = Modifier.padding(CARD_PADDING)) {
            Column {
                Text(
                    text = "Could not fetch posts, tap to retry",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (message != null) {
                    Spacer(modifier = Modifier.height(SPACER_SIZE))
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
private fun EndOfPaging() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SPACER_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "No more items")
    }
}