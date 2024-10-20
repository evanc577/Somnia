package dev.evanchang.somnia.api.reddit

import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.dto.RedditApiMeResponse
import dev.evanchang.somnia.api.reddit.dto.RedditAuthApiResponse

interface RedditLoginApi {
    suspend fun postAccessToken(
        clientId: String,
        redirectUri: String,
        code: String,
    ): ApiResult<RedditAuthApiResponse>

    suspend fun postRefreshAccessToken(
        clientId: String,
        redirectUri: String,
        refreshToken: String,
    ): ApiResult<RedditAuthApiResponse>

    suspend fun getApiV1Me(
        accessToken: String,
    ): ApiResult<RedditApiMeResponse>
}