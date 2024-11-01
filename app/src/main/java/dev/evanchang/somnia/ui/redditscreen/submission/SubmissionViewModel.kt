package dev.evanchang.somnia.ui.redditscreen.submission

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionViewModel(
    initialSubmission: Submission?,
    val submissionId: String,
    val sort: CommentSort,
) {
    private var _submission = MutableStateFlow(initialSubmission)
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
                _comments.value = walkComments(r.value.comments).toPersistentList()
            }
        }

        return ApiResult.Ok(Unit)
    }

    private fun walkComments(commentsTree: List<Comment>): Sequence<Comment> {
        return sequence {
            for (comment in commentsTree) {
                yield(comment)
                if (comment.replies != null && comment.replies is RedditResponse.Listing) {
                    yieldAll(
                        walkComments(comment.replies.data.children.filterIsInstance<Thing.CommentThing>()
                            .map { ct -> ct.comment })
                    )
                }
            }
        }
    }

    private var _mediaViewerState: MutableStateFlow<MediaViewerState> =
        MutableStateFlow(MediaViewerState.NotShowing)
    val mediaViewerState = _mediaViewerState.asStateFlow()

    fun setMediaViewerState(mediaViewerState: MediaViewerState) {
        _mediaViewerState.value = mediaViewerState
    }
}