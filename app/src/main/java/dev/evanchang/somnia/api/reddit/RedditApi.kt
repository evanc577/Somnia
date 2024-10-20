package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort

interface RedditApi {
    suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: SubmissionSort,
        after: String = "",
        limit: Int = 100,
    ): ApiResult<List<Submission>>
}