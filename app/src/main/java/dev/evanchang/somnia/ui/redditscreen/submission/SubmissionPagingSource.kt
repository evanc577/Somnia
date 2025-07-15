package dev.evanchang.somnia.ui.redditscreen.submission

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.flatten

class SubmissionPagingSource(
    private val submissionId: String,
    private val sort: CommentSort,
) : PagingSource<List<String>, Comment>() {
    override suspend fun load(params: LoadParams<List<String>>): LoadResult<List<String>, Comment> {
        val key = params.key
        if (key == null) {
            // If key is null, this is the first load
            return when (val r = RedditApiInstance.api.getSubmission(
                submissionId = submissionId,
                commentSort = sort,
                parentId = null,
            )) {
                is ApiResult.Ok -> {
                    var comments = r.value.comments.flatten().toMutableList()
                    val topLevelMore = comments.lastOrNull()
                    val nextKey = if (topLevelMore == null || topLevelMore !is Comment.More) {
                        null
                    } else {
                        comments.removeAt(comments.lastIndex)
                        topLevelMore.children
                    }
                    LoadResult.Page(
                        data = comments,
                        prevKey = null,
                        nextKey = nextKey,
                    )
                }

                is ApiResult.Err -> LoadResult.Error(Exception(r.message))
            }
        } else {
            val fetchIds = key.take(params.loadSize)
            val remainingIds = key.slice(fetchIds.size..key.lastIndex)

            return when (val r = RedditApiInstance.api.getMoreChildren(
                submissionId = "t3_$submissionId",
                children = fetchIds,
                sort = sort,
            )) {
                is ApiResult.Ok -> LoadResult.Page(
                    data = r.value.comments.flatten().toList(),
                    prevKey = null,
                    nextKey = if (r.value.comments.isEmpty()) {
                        null
                    } else {
                        remainingIds
                    },
                )

                is ApiResult.Err -> LoadResult.Error(Exception(r.message)) }
        }
    }

    override fun getRefreshKey(state: PagingState<List<String>, Comment>): List<String>? {
        return null
    }
}