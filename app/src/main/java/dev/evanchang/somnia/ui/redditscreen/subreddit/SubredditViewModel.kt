package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class SubredditViewModel(val subreddit: String, val defaultSort: SubmissionSort) : ViewModel() {
    private val sort = MutableStateFlow(defaultSort)

    @OptIn(ExperimentalCoroutinesApi::class)
    val submissions = sort.flatMapLatest { sort ->
        Pager(
            config = PagingConfig(pageSize = 3),
            pagingSourceFactory = {
                SubredditPagingSource(
                    subreddit = subreddit,
                    sort = sort,
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    private var _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun updateIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun updateSort(newSort: SubmissionSort) {
        sort.value = newSort
    }

    private var _mediaViewerState: MutableStateFlow<MediaViewerState> =
        MutableStateFlow(MediaViewerState.NotShowing)
    val mediaViewerState = _mediaViewerState.asStateFlow()

    fun setMediaViewerState(mediaViewerState: MediaViewerState) {
        _mediaViewerState.value = mediaViewerState
    }
}