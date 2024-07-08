package dev.evanchang.somnia.api.reddit

import androidx.annotation.Keep
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.annotations.SerializedName
import dev.evanchang.somnia.data.Submission
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
private data class ApiResponse(
    @SerializedName("data") var responseData: ResponseData,
)

@Keep
private data class ResponseData(
    @SerializedName("after") var after: String,
    @SerializedName("children") var children: List<ResponseSubmissionWrapper>,
)

@Keep
private data class ResponseSubmissionWrapper(
    @SerializedName("data") var submission: Submission,
)

private interface API {
    @GET("r/{subreddit}/{sort}")
    suspend fun getSubmissions(
        @Path("subreddit") subreddit: String,
        @Path("sort") sort: String,
        @Query("after") after: String?,
        @Query("limit") limit: Int?,
    ): Response<ApiResponse>

    companion object {
        var api: API? = null

        fun getInstance(): API {
            if (api == null) {
                val retrofit = Retrofit.Builder().baseUrl("https://api.reddit.com")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                api = retrofit.create(API::class.java)
            }
            return api!!
        }
    }
}

class SubredditSubmissionsPagingSource : PagingSource<String, Submission>() {
    private val backend: API = API.getInstance()

    override suspend fun load(
        params: LoadParams<String>
    ): LoadResult<String, Submission> {
        try {
            val response = backend.getSubmissions(
                subreddit = "dreamcatcher",
                sort = "new",
                after = params.key,
                limit = params.loadSize
            )
            val submissions: List<Submission> =
                response.body()!!.responseData.children.map { d -> d.submission }
            return LoadResult.Page(
                data = submissions, prevKey = null, nextKey = submissions.last().id
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Submission>): String? {
        return null
    }
}