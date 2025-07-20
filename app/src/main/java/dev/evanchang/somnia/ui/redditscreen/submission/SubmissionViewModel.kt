package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionViewModel(
    initialSubmission: Submission?,
    val submissionId: String,
    val sort: CommentSort,
) : ViewModel() {
    val submission = mutableStateOf(initialSubmission)

    val comments = Pager(PagingConfig(pageSize = 100)) {
        SubmissionPagingSource(
            submissionId = submissionId,
            sort = sort,
        )
    }.flow.cachedIn(viewModelScope)

    private var _mediaViewerState: MutableStateFlow<MediaViewerState> =
        MutableStateFlow(MediaViewerState.NotShowing)
    val mediaViewerState = _mediaViewerState.asStateFlow()

    fun setMediaViewerState(mediaViewerState: MediaViewerState) {
        _mediaViewerState.value = mediaViewerState
    }

    class Factory(
        private val initialSubmission: Submission?,
        private val submissionId: String,
        private val sort: CommentSort,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SubmissionViewModel(initialSubmission, submissionId, sort) as T
        }
    }
}