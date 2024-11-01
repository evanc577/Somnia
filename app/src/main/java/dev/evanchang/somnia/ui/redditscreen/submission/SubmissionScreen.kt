package dev.evanchang.somnia.ui.redditscreen.submission

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evanchang.markdown.MarkdownText
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.ui.UiConstants.BODY_TEXT_PADDING
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import dev.evanchang.somnia.ui.navigation.HorizontalDraggableScreen
import dev.evanchang.somnia.ui.navigation.NavigationViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel
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

    val context = LocalContext.current

    val submission by submissionViewModel.submission.collectAsStateWithLifecycle()
    val comments by submissionViewModel.comments.collectAsStateWithLifecycle()

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

                    items(
                        count = comments.size,
                        key = { index ->
                            comments[index].name
                        },
                    ) { index ->
                        val comment = comments[index]
                        Spacer(modifier = Modifier.height(1.dp))
                        Card {
                            Column {
                                Row {
                                    Text(text = "u/${comment.author}")
                                }
                                MarkdownText(
                                    markdownText = comment.body,
                                    style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                                    highlightColor = MaterialTheme.colorScheme.primary,
                                    onLinkClick = {
                                        // TODO handle markdown link
                                        Toast.makeText(context, "TODO: $it", Toast.LENGTH_SHORT)
                                            .show()
                                    },
                                    modifier = Modifier
                                        .padding(BODY_TEXT_PADDING)
                                        .fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}