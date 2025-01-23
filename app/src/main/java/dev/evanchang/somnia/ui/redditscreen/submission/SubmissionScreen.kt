package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.ui.UiConstants.BODY_TEXT_PADDING
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import dev.evanchang.somnia.ui.navigation.HorizontalDraggableScreen
import dev.evanchang.somnia.ui.navigation.NavigationViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel
import dev.evanchang.somnia.ui.util.SomniaMarkdown
import dev.evanchang.somnia.ui.util.SubmissionCard
import dev.evanchang.somnia.ui.util.SubmissionCardMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubmissionScreen(
    appSettings: AppSettings,
    screenStackIndex: Int,
    navigationViewModel: NavigationViewModel,
    submissionViewModel: SubmissionViewModel,
) {
    LaunchedEffect(Unit) {
        submissionViewModel.loadInitial()
    }

    val submission by submissionViewModel.submission.collectAsStateWithLifecycle()
    val comments by submissionViewModel.comments.collectAsStateWithLifecycle()
    val topCommentDepth = comments.firstOrNull()?.depth ?: 0

    // Media viewer
    val mediaViewerState = submissionViewModel.mediaViewerState.collectAsStateWithLifecycle()
    when (val s = mediaViewerState.value) {
        is MediaViewerState.Showing -> {
            MediaViewer(
                submission = s.submission,
                screenSize = navigationViewModel.screenSize.value,
                onClose = {
                    submissionViewModel.setMediaViewerState(MediaViewerState.NotShowing)
                },
            )
        }

        else -> {}
    }

    HorizontalDraggableScreen(
        screenStackIndex = screenStackIndex,
        navigationViewModel = navigationViewModel,
    ) {
        val submissionVal = submission
        Scaffold { padding ->
            if (submissionVal != null) {
                LazyColumn(
                    state = rememberLazyListState(prefetchStrategy = LazyListPrefetchStrategy(100)),
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    // Submission body
                    item(key = submissionVal.name) {
                        SubmissionCard(
                            submission = submissionVal,
                            mode = SubmissionCardMode.DETAILS,
                            onClickSubreddit = { subreddit ->
                                navigationViewModel.pushSubredditScreen(
                                    SubredditViewModel(
                                        subreddit,
                                        appSettings.generalSettings.defaultSubmissionSort,
                                    )
                                )
                            },
                            setShowMediaViewerState = { submissionViewModel.setMediaViewerState(it) },
                        )
                    }

                    // Comments
                    items(
                        count = comments.size,
                        key = { index ->
                            comments[index].name
                        },
                    ) { index ->
                        val comment = comments[index]
                        CommentItem(
                            comment = comment,
                            baseDepth = topCommentDepth,
                        )
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
) {
    val density = LocalDensity.current
    val scoreTimeSepColor = MaterialTheme.colorScheme.onSurface
    var height by remember { mutableStateOf(0.dp) }

    Row(modifier = Modifier.onSizeChanged { with(density) { height = it.height.toDp() } }) {
        // Indent guides
        Row {
            repeat(comment.depth - baseDepth) {
                VerticalDivider(
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(height),
                )
            }
        }

        // Comment
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
}


@Preview
@Composable
private fun CommentItemPreview() {
    val comment = Comment(
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
        CommentItem(comment, 0)
    }
}