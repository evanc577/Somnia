package dev.evanchang.somnia.api.reddit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.data.Submission

class SubredditSubmissionsPagingSource : PagingSource<String, Submission>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Submission> {
        return when (val r = RedditApiInstance.api.getSubredditSubmissions(
            subreddit = "dreamcatcher",
            sort = "new",
            after = params.key ?: "",
        )) {
            is ApiResult.Ok -> LoadResult.Page(
                data = r.value,
                prevKey = null,
                nextKey = r.value.lastOrNull()?.id,
            )

            is ApiResult.Err -> LoadResult.Error(Exception(r.message))
        }
    }

    override fun getRefreshKey(state: PagingState<String, Submission>): String? {
        return null
    }
}