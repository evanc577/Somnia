package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.mediaViewer.MediaViewer
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import dev.evanchang.somnia.ui.navigation.HorizontalDraggableScreen
import dev.evanchang.somnia.ui.navigation.NavigationViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel
import dev.evanchang.somnia.ui.util.SubmissionCard
import dev.evanchang.somnia.ui.util.SubmissionCardMode

@Composable
fun SubmissionScreen(
    screenStackIndex: Int,
    navigationViewModel: NavigationViewModel,
    submissionViewModel: SubmissionViewModel,
) {
    LaunchedEffect(Unit) {
        submissionViewModel.loadInitial()
    }

    val submission by submissionViewModel.submission.collectAsStateWithLifecycle()

    // Media viewer
    val mediaViewerState = submissionViewModel.mediaViewerState.collectAsStateWithLifecycle()
    when (val s = mediaViewerState.value) {
        is MediaViewerState.Showing -> {
            MediaViewer(
                submission = s.submission,
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
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    item(key = submissionVal.name) {
                        SubmissionCard(
                            submission = submissionVal,
                            mode = SubmissionCardMode.DETAILS,
                            onClickSubreddit = { subreddit ->
                                navigationViewModel.pushSubredditScreen(SubredditViewModel(subreddit, SubmissionSort.New))
                            },
                            setShowMediaViewerState = { submissionViewModel.setMediaViewerState(it) },
                        )
                    }
                }
            }
        }
    }
}