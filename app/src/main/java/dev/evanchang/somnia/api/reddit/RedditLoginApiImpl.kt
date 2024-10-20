package dev.evanchang.somnia.api.reddit

import android.util.Log
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.UnauthenticatedHttpClient
import dev.evanchang.somnia.api.reddit.dto.RedditApiMeResponse
import dev.evanchang.somnia.api.reddit.dto.RedditAuthApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Parameters
import io.ktor.http.isSuccess

class RedditLoginApiImpl(private val client: HttpClient) : RedditLoginApi {
    override suspend fun postAccessToken(
        clientId: String,
        redirectUri: String,
        code: String,
    ): ApiResult<RedditAuthApiResponse> {
        val response = try {
            client.post {
                url("https://www.reddit.com/api/v1/access_token")
                headers {
                    basicAuth(username = clientId, password = "")
                }
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", redirectUri)
                }))
            }
        } catch (e: Exception) {
            return ApiResult.Err(e.toString())
        }

        if (!response.status.isSuccess()) {
            return ApiResult.Err("bad status: ${response.status.value}")
        }

        val body: RedditAuthApiResponse = try {
            response.body()
        } catch (e: Exception) {
            return ApiResult.Err("body parse error")
        }

        return ApiResult.Ok(body)
    }

    override suspend fun postRefreshAccessToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
    ): ApiResult<RedditAuthApiResponse> {
        val response = try {
            client.post {
                url("https://www.reddit.com/api/v1/access_token")
                headers {
                    basicAuth(username = clientId, password = "")
                }
                setBody(FormDataContent(Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                    append("redirect_uri", redirectUri)
                }))
            }
        } catch (e: Exception) {
            return ApiResult.Err(e.toString())
        }

        if (!response.status.isSuccess()) {
            return ApiResult.Err("bad status: ${response.status.value}")
        }

        val body: RedditAuthApiResponse = try {
            response.body()
        } catch (e: Exception) {
            return ApiResult.Err("body parse error")
        }

        return ApiResult.Ok(body)
    }


    override suspend fun getApiV1Me(accessToken: String): ApiResult<RedditApiMeResponse> {
        val response = try {
            client.get {
                url("https://oauth.reddit.com/api/v1/me")
                headers {
                    bearerAuth(accessToken)
                }
            }
        } catch (e: Exception) {
            return ApiResult.Err(e.toString())
        }

        val parsedResponse: RedditApiMeResponse = response.body()

        return ApiResult.Ok(parsedResponse)
    }
}

object RedditLoginApiInstance {
    val api: RedditLoginApiImpl by lazy {
        RedditLoginApiImpl(UnauthenticatedHttpClient.client)
    }
}