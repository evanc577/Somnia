package dev.evanchang.somnia.api.reddit

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface RedditAuthApi {
    @FormUrlEncoded
    @POST("api/v1/access_token")
    suspend fun postAccessToken(
        @Header("authorization") authorization: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
    ): Response<RedditAuthApiResponse>
}

object RedditAuthApiInstance {
    private const val BASE_URL = "https://www.reddit.com/"
    val api: RedditAuthApi by lazy {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditAuthApi::class.java)
    }
}

@Keep
data class RedditAuthApiResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("scope") val scope: String,
    @SerializedName("refresh_token") val refreshToken: String,
)