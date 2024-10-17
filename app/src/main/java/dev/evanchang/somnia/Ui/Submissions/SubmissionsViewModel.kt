package dev.evanchang.somnia.Ui.Submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.evanchang.somnia.Api.reddit.SubredditSubmissionsPagingSource
import dev.evanchang.somnia.Data.Submission
import kotlinx.coroutines.flow.Flow

class SubmissionsViewModel : ViewModel() {
    val submissions: Flow<PagingData<Submission>> = Pager(PagingConfig(pageSize = 3)) {
        SubredditSubmissionsPagingSource()
    }.flow.cachedIn(viewModelScope)
}