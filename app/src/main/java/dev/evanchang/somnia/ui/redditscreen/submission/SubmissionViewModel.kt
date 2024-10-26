package dev.evanchang.somnia.ui.redditscreen.submission

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionViewModel(
    val submissionId: String,
    val sort: CommentSort,
) {
    private var _submission = MutableStateFlow<Submission?>(null)
    val submission = _submission.asStateFlow()

    private var _comments = MutableStateFlow<PersistentList<Comment>>(persistentListOf())
    val comments = _comments.asStateFlow()

    suspend fun loadInitial(): ApiResult<Unit> {
        val response = RedditApiInstance.api.getSubmission(
            submissionId = submissionId,
            commentSort = sort,
            parentId = null,
        )

        when (val r = response) {
            is ApiResult.Err -> return ApiResult.Err(r.message)
            is ApiResult.Ok -> {
                _submission.value = r.value.submission
                _comments.value = r.value.comments.toPersistentList()
            }
        }

        return ApiResult.Ok(Unit)
    }
}