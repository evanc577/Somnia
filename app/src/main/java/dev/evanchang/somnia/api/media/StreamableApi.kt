package dev.evanchang.somnia.api.media

import android.util.Log
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.doRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class StreamableApi {
    private val protocol = URLProtocol.HTTPS
    private val host = "api.streamable.com"

    suspend fun getVideo(id: String): ApiResult<MediaItem> {
        val response = doRequest<StreamableResponse> {
            StreamableApiClient.client.get {
                url {
                    protocol = this@StreamableApi.protocol
                    host = this@StreamableApi.host
                    appendPathSegments("videos")
                    appendPathSegments(id)
                }
            }
        }

        return when (response) {
            is ApiResult.Err -> response

            is ApiResult.Ok -> ApiResult.Ok(
                MediaItem(
                    mediaType = MediaType.Video,
                    url = response.value.files.mp4.url,
                    width = response.value.files.mp4.width,
                    height = response.value.files.mp4.height,
                )
            )
        }
    }
}

@Serializable
private class StreamableResponse(
    val url: String,
    val files: Files,
)

@Serializable
private class Files(
    val mp4: Video,
    @SerialName("mp4-mobile") val mp4Mobile: Video,
)

@Serializable
private class Video(
    val url: String,
    val framerate: Int,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val size: Int,
    val duration: Float,
)

private object StreamableApiClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("ktor", message)
                }
            }
            level = LogLevel.ALL
        }
    }
}