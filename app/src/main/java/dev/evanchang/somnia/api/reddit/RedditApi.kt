package dev.evanchang.somnia.api.reddit

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import dev.evanchang.somnia.data.Submission
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApi {
    @GET("r/{subreddit}/{sort}")
    suspend fun getSubmissions(
        @Path("subreddit") subreddit: String,
        @Path("sort") sort: String,
        @Query("after") after: String?,
        @Query("limit") limit: Int?,
    ): Response<ApiResponse>

    companion object {
        var api: RedditApi? = null
        private const val BASE_URL = "https://api.reddit.com/"

        fun getInstance(): RedditApi {
            if (api == null) {
                val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                api = retrofit.create(RedditApi::class.java)
            }
            return api!!
        }
    }
}

@Keep
data class ApiResponse(
    @SerializedName("data") var responseData: ResponseData,
)

@Keep
data class ResponseData(
    @SerializedName("after") var after: String,
    @SerializedName("children") var children: List<ResponseSubmissionWrapper>,
)

@Keep
data class ResponseSubmissionWrapper(
    @SerializedName("data") var submission: Submission,
)