package dev.evanchang.somnia.api.reddit

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import dev.evanchang.somnia.data.Submission
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApi {
    @GET("api/v1/me")
    suspend fun getApiV1Me(
        @Header("authorization") authorization: String,
    ): Response<ApiV1MeResponse>

    @GET("r/{subreddit}/{sort}.json")
    suspend fun getSubredditSubmissions(
        @Header("authorization") authorization: String? = null,
        @Path("subreddit") subreddit: String,
        @Path("sort") sort: String,
        @Query("after") after: String?,
        @Query("limit") limit: Int?,
    ): Response<SubredditSubmissionsResponse>
}

object RedditApiInstance {
    private const val BASE_URL = "https://oauth.reddit.com/"
    val api: RedditApi by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApi::class.java)
    }
}

@Keep
data class SubredditSubmissionsResponse(
    @SerializedName("data") val responseData: SubredditSubmissionsResponseData,
) {
    @Keep
    data class SubredditSubmissionsResponseData(
        @SerializedName("after") val after: String,
        @SerializedName("children") val children: List<SubredditSubmissionsResponseSubmissionWrapper>,
    ) {
        @Keep
        data class SubredditSubmissionsResponseSubmissionWrapper(
            @SerializedName("data") val submission: Submission,
        )
    }
}

@Keep
data class ApiV1MeResponse(
    val name: String,
)