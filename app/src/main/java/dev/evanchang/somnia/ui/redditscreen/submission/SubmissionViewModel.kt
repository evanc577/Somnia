package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubmissionViewModel(
    initialSubmission: Submission?,
    val submissionId: String,
    settings: Flow<AppSettings>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val SAVED_SORT_KEY = "sort"
    private val sort: MutableStateFlow<CommentSort?> =
        MutableStateFlow(savedStateHandle.get<CommentSort>(SAVED_SORT_KEY))

    init {
        if (sort.value == null) {
            viewModelScope.launch {
                settings.map { settings ->
                    settings.generalSettings.defaultCommentSort
                }.collect { defaultSort ->
                    sort.value = defaultSort
                }
            }
        }
    }

    val submission = mutableStateOf(initialSubmission)

    @OptIn(ExperimentalCoroutinesApi::class)
    val comments = sort.flatMapLatest { sort ->
        Pager(
            config = PagingConfig(pageSize = 100),
            pagingSourceFactory = {
                SubmissionPagingSource(
                    submissionId = submissionId,
                    sort = sort,
                )
            }
        ).flow
    }.cachedIn(viewModelScope)

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun setIsRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun setSort(newSort: CommentSort) {
        sort.value = newSort
        savedStateHandle[SAVED_SORT_KEY] = newSort
    }

    class Factory(
        private val initialSubmission: Submission?,
        private val submissionId: String,
        private val settings: Flow<AppSettings>,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            @Suppress("UNCHECKED_CAST")
            return SubmissionViewModel(
                initialSubmission,
                submissionId,
                settings,
                extras.createSavedStateHandle(),
            ) as T
        }
    }
}