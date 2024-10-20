package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.api.reddit.dto.SubredditSubmissionsResponse
import dev.evanchang.somnia.data.Submission
import dev.evanchang.somnia.data.SubmissionSort
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess

class RedditApiImpl(private val client: HttpClient) : RedditApi {
    private val PROTOCOL = URLProtocol.HTTPS
    private val HOST = "oauth.reddit.com"

    override suspend fun getSubredditSubmissions(
        subreddit: String,
        sort: SubmissionSort,
        after: String,
        limit: Int,
    ): ApiResult<List<Submission>> {
        val sortString = sort.toString()
        val response = try {
            client.get {
                url {
                    protocol = PROTOCOL
                    host = HOST
                    appendPathSegments("r", subreddit, encodeSlash = true)
                    appendPathSegments(sortString, encodeSlash = false)
                    appendPathSegments(".json")
                    parameters.append("after", after)
                    parameters.append("limit", limit.toString())
                }
            }
        } catch (e: Exception) {
            return ApiResult.Err(e.toString())
        }

        if (!response.status.isSuccess()) {
            return ApiResult.Err("bad status: ${response.status.value}")
        }

        val body: SubredditSubmissionsResponse = try {
            response.body()
        } catch (e: Exception) {
            return ApiResult.Err(e.toString())
        }

        return ApiResult.Ok(body.responseData.children.map { data -> data.submission })
    }
}

object RedditApiInstance {
    val api: RedditApiImpl by lazy {
        RedditApiImpl(RedditHttpClient.client)
    }
}
