package dev.evanchang.somnia.Api.reddit

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.evanchang.somnia.Data.Submission

class SubredditSubmissionsPagingSource : PagingSource<String, Submission>() {
    private val backend: RedditApi = RedditApi.getInstance()

    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Submission> {
        try {
            val response = backend.getSubmissions(
                subreddit = "dreamcatcher",
                sort = "new",
                after = params.key,
                limit = 100,
            )
            val submissions: List<Submission> =
                response.body()!!.responseData.children.map { d -> d.submission }
            return LoadResult.Page(
                data = submissions, prevKey = null, nextKey = submissions.last().id
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Submission>): String? {
        return null
    }
}