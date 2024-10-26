package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.data.Comment
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort

interface RedditApi {
    suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: SubmissionSort,
        after: String = "",
        limit: Int = 100,
    ): ApiResult<SubredditSubmissionsResponse>

    class SubredditSubmissionsResponse(
        val submissions: List<Submission>,
        val after: String?,
    )

    suspend fun getSubmission(
        submissionId: String,
        parentId: String?,
        commentSort: CommentSort,
        after: String = "",
        limit: Int = 100,
    ): ApiResult<SubmissionResponse>

    class SubmissionResponse(
        val submission: Submission,
        val comments: List<Comment>,
        val commentsAfter: String?,
    )
}