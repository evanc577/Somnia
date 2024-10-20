package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.data.Submission

interface RedditApi {
    suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: String,
        after: String = "",
        limit: Int = 100,
    ): ApiResult<List<Submission>>
}