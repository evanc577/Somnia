package dev.evanchang.somnia.ui.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.evanchang.somnia.api.reddit.SubredditSubmissionsPagingSource
import dev.evanchang.somnia.data.Submission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionsViewModel : ViewModel() {
    val submissions: Flow<PagingData<Submission>> = Pager(PagingConfig(pageSize = 3)) {
        SubredditSubmissionsPagingSource()
    }.flow.cachedIn(viewModelScope)
}