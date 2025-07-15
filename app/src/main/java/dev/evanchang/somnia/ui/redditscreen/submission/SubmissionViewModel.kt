package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.ui.mediaViewer.MediaViewerState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubmissionViewModel(
    initialSubmission: Submission?,
    val submissionId: String,
    val sort: CommentSort,
) {
    val submission = mutableStateOf(initialSubmission)

    val comments = mutableStateListOf<Comment>()

    suspend fun loadInitial(): ApiResult<Unit> {
        val response = RedditApiInstance.api.getSubmission(
            submissionId = submissionId,
            commentSort = sort,
            parentId = null,
        )

        when (response) {
            is ApiResult.Err -> return response
            is ApiResult.Ok -> {
                submission.value = response.value.submission
                comments.apply {
                    this.addAll(flattenComments(response.value.comments))
                }
            }
        }

        return ApiResult.Ok(Unit)
    }

    suspend fun loadMore(name: String): ApiResult<Unit> {
        val index = comments.indexOfFirst {
            it.name() == name
        }
        if (index == -1) {
            return ApiResult.Ok(Unit)
        }

        val more = when (val comment = comments[index]) {
            is Comment.More -> comment
            else -> return ApiResult.Ok(Unit)
        }

        val children = more.children.take(10)
        val response = RedditApiInstance.api.getMoreChildren(
            submissionId = "t3_$submissionId",
            children = children,
            sort = sort,
        )

        when (response) {
            is ApiResult.Err -> return response
            is ApiResult.Ok -> {
                val newComments = flattenComments(response.value.comments)
                val newCommentIds = newComments.map { it.id() }.toHashSet()
                more.children = more.children.removeAll { newCommentIds.contains(it) }.toPersistentList()
                if (more.children.isEmpty()) {
                    comments.removeAt(index)
                }
                comments.addAll(index, newComments.toList())
            }
        }

        return ApiResult.Ok(Unit)
    }

    private fun flattenComments(commentsTree: List<Comment>): Sequence<Comment> {
        return sequence {
            for (comment in commentsTree) {
                yield(comment)
                when (comment) {
                    is Comment.CommentData -> if (comment.replies != null && comment.replies is RedditResponse.Listing) {
                        yieldAll(
                            flattenComments(
                                comment.replies.data.children.filterIsInstance<Thing.CommentThing>()
                                    .map { ct -> ct.comment })
                        )
                    }

                    is Comment.More -> {}
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