package dev.evanchang.somnia.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.redditscreen.submission.SubmissionViewModel
import dev.evanchang.somnia.ui.redditscreen.subreddit.SubredditViewModel

data class NavigationUIState(
    private val defaultSubmissionSort: SubmissionSort,
    val navigationBackStack: SnapshotStateList<NavigationBackStackEntry> = mutableStateListOf(
        NavigationBackStackEntry.SubredditBackStackEntry(
            viewModel = SubredditViewModel(
                subreddit = "dreamcatcher",
                sort = defaultSubmissionSort,
            )
        )
    ),
)

sealed class NavigationBackStackEntry {
    class SubredditBackStackEntry(
        val viewModel: SubredditViewModel
    ) : NavigationBackStackEntry()
    class SubmissionBackStackEntry(
        val viewModel: SubmissionViewModel
    ) : NavigationBackStackEntry()
}