package dev.evanchang.somnia.ui.submissions

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsPagingSourceFactory
import dev.evanchang.somnia.data.Submission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionsListViewModel(val subreddit: String) : ViewModel() {
    private var pagingSourceFactory =
        SubredditSubmissionsPagingSourceFactory(subreddit = subreddit, sort = "new")
    val submissions: Flow<PagingData<Submission>> = Pager(PagingConfig(pageSize = 3)) {
        pagingSourceFactory.invoke()
    }.flow.cachedIn(viewModelScope)
    private var _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun updateIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun updateSort(sort: String) {
        pagingSourceFactory =
            SubredditSubmissionsPagingSourceFactory(subreddit = subreddit, sort = sort)
    }
}

class SubmissionsListViewModelFactory(val subreddit: String) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubmissionsListViewModel(subreddit) as T
    }
}