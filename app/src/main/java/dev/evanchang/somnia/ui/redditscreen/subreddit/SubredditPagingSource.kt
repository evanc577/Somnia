package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.evanchang.somnia.api.WaitForDataStore
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort

class SubredditPagingSource(
    private val subreddit: String,
    private val sort: SubmissionSort?,
) :
    PagingSource<String, Submission>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Submission> {
        // Sort may come async from datastore, return nothing if it hasn't loaded yet
        if (sort == null) {
            return LoadResult.Error(WaitForDataStore)
        }

        return when (val r = RedditApiInstance.api.getSubredditSubmissions(
            subreddit = subreddit,
            sort = sort,
            after = params.key ?: "",
        )) {
            is ApiResult.Ok -> LoadResult.Page(
                data = r.value.submissions,
                prevKey = null,
                nextKey = r.value.after,
            )

            is ApiResult.Err -> LoadResult.Error(Exception(r.message))
        }
    }

    override fun getRefreshKey(state: PagingState<String, Submission>): String? {
        return null
    }
}