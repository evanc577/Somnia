package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.data.SubmissionSort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubredditViewModel(val subreddit: String, settings: Flow<AppSettings>) : ViewModel() {
    private val sort: MutableStateFlow<SubmissionSort?> =
        MutableStateFlow(null)

    init {
        viewModelScope.launch {
            settings.map { settings ->
                settings.generalSettings.defaultSubmissionSort
            }.collect { defaultSort ->
                sort.value = defaultSort
            }
        }
    }

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

    fun setIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun setSort(newSort: SubmissionSort) {
        sort.value = newSort
    }

    class Factory(private val subreddit: String, private val settings: Flow<AppSettings>) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SubredditViewModel(subreddit, settings) as T
        }
    }
}