package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.api.reddit.dto.RedditResponse
import dev.evanchang.somnia.api.reddit.dto.Thing
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
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
    ): ApiResult<Pair<List<Submission>, String?>> {
        val sortString = sort.toString()
        val response = doRequest<RedditResponse> {
            client.get {
                url {
                    protocol = this@RedditApiImpl.protocol
                    host = this@RedditApiImpl.host
                    appendPathSegments("r", subreddit, encodeSlash = true)
                    appendPathSegments(sortString, encodeSlash = false)
                    appendPathSegments(".json")
                    parameters.append("after", after)
                    parameters.append("limit", limit.toString())
                }
            }
        }

        return when (response) {
            is ApiResult.Ok -> {
                if (response.value is RedditResponse.Listing) {
                    val submissions =
                        response.value.data.children.filterIsInstance<Thing.SubmissionThing>()
                            .map { it.submission }
                    ApiResult.Ok(Pair(submissions, response.value.after))
                } else {
                    ApiResult.Ok(Pair(listOf(), null))
                }
            }

            is ApiResult.Err -> response
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
        response.body()
    } catch (e: Exception) {
        return ApiResult.Err(e.toString())
    }

    return ApiResult.Ok(body)
}