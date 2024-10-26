package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsPagingSourceFactory
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubredditViewModel(val subreddit: String, sort: SubmissionSort) : ViewModel() {
    private var pagingSourceFactory =
        SubredditSubmissionsPagingSourceFactory(subreddit = subreddit, sort = sort)

    val submissions: Flow<PagingData<Submission>> = Pager(PagingConfig(pageSize = 3)) {
        pagingSourceFactory.invoke()
    }.flow.cachedIn(viewModelScope)

    private var _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    sealed class MediaViewerState {
        data object NotShowing : MediaViewerState()
        class Showing(val submission: Submission) : MediaViewerState()
    }

    private var _mediaViewerState: MutableStateFlow<MediaViewerState> =
        MutableStateFlow(MediaViewerState.NotShowing)
    val mediaViewerState = _mediaViewerState.asStateFlow()

    fun updateIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun updateSort(sort: SubmissionSort) {
        pagingSourceFactory =
            SubredditSubmissionsPagingSourceFactory(subreddit = subreddit, sort = sort)
    }

    fun setMediaViewerState(mediaViewerState: MediaViewerState) {
        _mediaViewerState.value = mediaViewerState
    }
}