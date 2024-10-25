package dev.evanchang.somnia.api.reddit

import androidx.paging.PagingSource
import androidx.paging.PagingSourceFactory
import androidx.paging.PagingState
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort

class SubredditSubmissionsPagingSource(
    private val subreddit: String,
    private val sort: SubmissionSort,
) :
    PagingSource<String, Submission>() {
    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Submission> {
        return when (val r = RedditApiInstance.api.getSubredditSubmissions(
            subreddit = subreddit,
            sort = sort,
            after = params.key ?: "",
        )) {
            is ApiResult.Ok -> LoadResult.Page(
                data = r.value.first,
                prevKey = null,
                nextKey = r.value.second,
            )

            is ApiResult.Err -> LoadResult.Error(Exception(r.message))
        }
    }

    override fun getRefreshKey(state: PagingState<String, Submission>): String? {
        return null
    }
}

class SubredditSubmissionsPagingSourceFactory(
    private val subreddit: String,
    private val sort: SubmissionSort,
) :
    PagingSourceFactory<String, Submission> {
    override fun invoke(): PagingSource<String, Submission> {
        return SubredditSubmissionsPagingSource(subreddit = subreddit, sort = sort)
    }
}