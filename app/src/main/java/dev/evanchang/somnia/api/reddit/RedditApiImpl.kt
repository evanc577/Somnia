package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.api.doRequest
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.data.CommentSort
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.data.durationString
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable

class RedditApiImpl(private val client: HttpClient) : RedditApi {
    private val protocol = URLProtocol.HTTPS
    private val host = "oauth.reddit.com"

    override suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: SubmissionSort,
        after: String,
        limit: Int,
    ): ApiResult<RedditApi.SubredditSubmissionsResponse> {
        val sortDuration = sort.durationString()
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
                    if (sortDuration != null) {
                        parameters.append("t", sortDuration)
                    }
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
                val comments = commentsListing.data.children.mapNotNull {
                    when (it) {
                        is Thing.CommentThing -> it.comment
                        is Thing.MoreThing -> it.more
                        is Thing.SubmissionThing -> null
                    }
                }

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

    override suspend fun getMoreChildren(
        submissionId: String, children: List<String>, sort: CommentSort
    ): ApiResult<RedditApi.MoreChildrenResponse> {
        @Serializable
        data class MoreChildrenData(
            val things: List<Thing>,
        )

        @Serializable
        data class MoreChildrenJson(
            val data: MoreChildrenData,
        )

        @Serializable
        data class MoreChildrenResponse(
            val json: MoreChildrenJson,
        )

        val response = doRequest<MoreChildrenResponse> {
            client.get {
                url {
                    protocol = this@RedditApiImpl.protocol
                    host = this@RedditApiImpl.host
                    appendPathSegments("api", "morechildren.json", encodeSlash = true)
                    parameters.append("sort", sort.toString())
                    parameters.append("api_type", "json")
                    parameters.append("limit_children", "false")
                    parameters.append("link_id", submissionId)
                    parameters.append("children", children.joinToString(","))
                    parameters.append("raw_json", "1")
                }
            }
        }

        return when (response) {
            is ApiResult.Err -> response

            is ApiResult.Ok -> ApiResult.Ok(
                RedditApi.MoreChildrenResponse(
                    comments = response.value.json.data.things.mapNotNull {
                        when (it) {
                            is Thing.CommentThing -> it.comment
                            is Thing.MoreThing -> null  // Ignore top level Mores
                            is Thing.SubmissionThing -> null
                        }
                    }
                )
            )
        }
    }
}

object RedditApiInstance {
    val api: RedditApiImpl by lazy {
        RedditApiImpl(RedditHttpClient.client)
    }
}