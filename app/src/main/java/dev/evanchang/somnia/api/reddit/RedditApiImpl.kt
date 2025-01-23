package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.SubmissionSort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess

class RedditApiImpl(private val client: HttpClient) : RedditApi {
    private val protocol = URLProtocol.HTTPS
    private val host = "oauth.reddit.com"

    override suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: SubmissionSort,
        after: String,
        limit: Int,
    ): ApiResult<RedditApi.SubredditSubmissionsResponse> {
        val response = doRequest<RedditResponse> {
            client.get {
                url {
                    protocol = this@RedditApiImpl.protocol
                    host = this@RedditApiImpl.host
                    if (subreddit.isNotEmpty()) {
                        appendPathSegments("r", subreddit, encodeSlash = true)
                    }
                    appendPathSegments(sort.toString())
                    appendPathSegments(".json")
                    parameters.append("after", after)
                    parameters.append("limit", limit.toString())
                    parameters.append("raw_json", "1")
                }
            }
        }

        return when (response) {
            is ApiResult.Err -> response

            is ApiResult.Ok -> {
                if (response.value is RedditResponse.Listing) {
                    val submissions =
                        response.value.data.children.filterIsInstance<Thing.SubmissionThing>()
                            .map { it.submission }
                    ApiResult.Ok(
                        RedditApi.SubredditSubmissionsResponse(
                            submissions = submissions, after = response.value.data.after,
                        )
                    )
                } else {
                    ApiResult.Ok(
                        RedditApi.SubredditSubmissionsResponse(
                            submissions = listOf(), after = null
                        )
                    )
                }
            }
        }
    }

    override suspend fun getSubmission(
        submissionId: String,
        parentId: String?,
        commentSort: CommentSort,
        after: String,
        limit: Int,
    ): ApiResult<RedditApi.SubmissionResponse> {
        val response = doRequest<List<RedditResponse>> {
            client.get {
                url {
                    protocol = this@RedditApiImpl.protocol
                    host = this@RedditApiImpl.host
                    appendPathSegments("comments", submissionId, encodeSlash = true)
                    if (parentId != null) it.appendPathSegments("_", parentId)
                    appendPathSegments(".json")
                    parameters.append("sort", commentSort.toString())
                    parameters.append("after", after)
                    parameters.append("limit", limit.toString())
                    parameters.append("raw_json", "1")
                }
            }
        }

        return when (response) {
            is ApiResult.Err -> response

            is ApiResult.Ok -> {
                // Find submission
                val submission = response.value.filterIsInstance<RedditResponse.Listing>()
                    .firstOrNull()?.data?.children?.filterIsInstance<Thing.SubmissionThing>()
                    ?.firstOrNull()?.submission ?: return ApiResult.Err("no submission found")

                // Find comments
                val commentsListing =
                    response.value.filterIsInstance<RedditResponse.Listing>().getOrNull(1)
                        ?: return ApiResult.Err("no comments found")
                val commentsAfter = commentsListing.data.after
                val comments = commentsListing.data.children.filterIsInstance<Thing.CommentThing>()
                    .map { it.comment }

                ApiResult.Ok(
                    RedditApi.SubmissionResponse(
                        submission = submission,
                        comments = comments,
                        commentsAfter = commentsAfter,
                    )
                )
            }
        }
    }
}

object RedditApiInstance {
    val api: RedditApiImpl by lazy {
        RedditApiImpl(RedditHttpClient.client)
    }
}

private suspend inline fun <reified T> doRequest(request: () -> HttpResponse): ApiResult<T> {
    val response = try {
        request()
    } catch (e: Exception) {
        return ApiResult.Err(e.toString())
    }

    if (!response.status.isSuccess()) {
        return ApiResult.Err("bad status: ${response.status.value}")
    }

    val body: T = try {
        val text = response.bodyAsText()
        response.body()
    } catch (e: Exception) {
        return ApiResult.Err(e.toString())
    }

    return ApiResult.Ok(body)
}