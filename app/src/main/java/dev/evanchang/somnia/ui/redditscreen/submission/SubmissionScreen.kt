package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.evanchang.somnia.api.isWaitForDataStore
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.UiConstants.BODY_TEXT_PADDING
import dev.evanchang.somnia.ui.util.SomniaMarkdown
import dev.evanchang.somnia.ui.util.SubmissionCard
import dev.evanchang.somnia.ui.util.SubmissionCardMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    initialSubmission: Submission,
    submissionId: String,
) {
    val context = LocalContext.current

    val vm: SubmissionViewModel = viewModel(
        factory = SubmissionViewModel.Factory(
            initialSubmission = initialSubmission,
            submissionId = submissionId,
            settings = context.dataStore.data,
        )
    )
    val scope = rememberCoroutineScope()

    val submission by vm.submission
    val lazyCommentItems: LazyPagingItems<Comment> =
        vm.comments.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val topCommentDepth = remember {
        if (lazyCommentItems.itemCount == 0) {
            return@remember 0
        }
        val c = lazyCommentItems[0]
        if (c == null || c !is Comment.CommentData) {
            0
        } else {
            c.depth()
        }
    }

    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    val onRefresh: () -> Unit = remember {
        {
            vm.setIsRefreshing(true)
            scope.launch {
                lazyCommentItems.refresh()
            }
        }
    }

    LaunchedEffect(
        lazyCommentItems.loadState,
        isRefreshing,
    ) {
        if (lazyCommentItems.isWaitForDataStore()) {
            return@LaunchedEffect
        }
        if (isRefreshing && lazyCommentItems.loadState.refresh != LoadState.Loading) {
            vm.setIsRefreshing(false)
            listState.scrollToItem(0)
        }
    }


    Scaffold { padding ->
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
            modifier = Modifier.padding(top = padding.calculateTopPadding()),
        ) {
            val submission = submission
            if (submission != null) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Submission body
                    item(key = submission.name) {
                        SubmissionCard(
                            submission = submission,
                            mode = SubmissionCardMode.DETAILS,
                        )
                    }

                    // Comments
                    items(
                        count = lazyCommentItems.itemCount,
                        key = { index ->
                            lazyCommentItems[index]!!.name()
                        },
                    ) { index ->
                        val comment = lazyCommentItems[index]!!
                        CommentItem(
                            comment = comment,
                            baseDepth = topCommentDepth,
                            onMore = {
                                scope.launch {
//                                    submissionViewModel.loadMore(comment.name())
                                }
                            },
                        )
                    }

                    // Don't draw under nav bar
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    baseDepth: Int,
    onMore: (String) -> Unit,
) {
    val density = LocalDensity.current
    var height by remember { mutableStateOf(0.dp) }

    Row(modifier = Modifier.onSizeChanged { with(density) { height = it.height.toDp() } }) {
        // Indent guides
        Row {
            repeat(comment.depth() - baseDepth) {
                VerticalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(height),
                )
            }
            when (comment) {
                is Comment.CommentData -> CommentDisplay(comment)
                is Comment.More -> CommentMoreDisplay(comment, onClick = onMore)
            }
        }
    }
}

@Composable
private fun CommentDisplay(comment: Comment.CommentData) {
    val scoreTimeSepColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.padding(BODY_TEXT_PADDING)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "u/${comment.author}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            Text(
                text = if (comment.scoreHidden) {
                    "?"
                } else {
                    comment.score.toString()
                },
                style = MaterialTheme.typography.labelMedium,
            )
            Canvas(modifier = Modifier.padding(horizontal = 4.dp), onDraw = {
                drawCircle(
                    color = scoreTimeSepColor,
                    radius = 3f,
                )
            })
            Text(
                text = comment.elapsedTimeString(),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        SomniaMarkdown(
            content = comment.body,
            isPreview = false,
        )
    }
}

@Composable
private fun CommentMoreDisplay(more: Comment.More, onClick: (String) -> Unit) {
    Text(
        text = "Load more (${more.children.size}) (depth = ${more.depth})",
        modifier = Modifier.clickable { onClick(more.name) })
}

@Preview
@Composable
private fun CommentItemPreview() {
    val comment = Comment.CommentData(
        name = "1",
        id = "1",
        author = "author",
        permalink = "",
        body = "comment body",
        score = 2,
        scoreHidden = false,
        created = 0f,
        depth = 8,
        replies = null,
    )

    Surface {
        CommentItem(
            comment = comment,
            baseDepth = 0,
            onMore = {}
        )
    }
}