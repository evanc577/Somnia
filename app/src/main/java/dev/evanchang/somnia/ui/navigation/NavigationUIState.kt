package dev.evanchang.somnia.ui.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel

data class NavigationUIState(
    val navigationBackStack: SnapshotStateList<NavigationBackStackEntry> = mutableStateListOf(
        NavigationBackStackEntry.SubredditBackStackEntry(
            viewModel = SubmissionsListViewModel(
                subreddit = "dreamcatcher",
                sort = SubmissionSort.New,
            )
        )
    ),
)

sealed class NavigationBackStackEntry {
    class SubredditBackStackEntry(
        val viewModel: SubmissionsListViewModel
    ) : NavigationBackStackEntry()
}