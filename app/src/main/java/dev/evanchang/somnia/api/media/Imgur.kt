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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class Imgur {
    private val protocol = URLProtocol.HTTPS

    suspend fun getAlbum(id: String): ApiResult<ImmutableList<MediaItem>> {
        return callApi(id, ImgurApiType.Album)
    }

    suspend fun getMedia(id: String): ApiResult<ImmutableList<MediaItem>> {
        return callApi(id, ImgurApiType.Media)
    }

    private suspend fun getClientId(): ApiResult<String> {
        // Get main.js
        val htmlResponse = doRequest<String> {
            ImgurClient.client.get {
                url {
                    protocol = this@Imgur.protocol
                    host = "imgur.com"
                }
            }
        }
        val htmlText = when (htmlResponse) {
            is ApiResult.Ok -> htmlResponse.value
            is ApiResult.Err -> return htmlResponse
        }
        val m = ImgurClient.mainJsRe.find(htmlText) ?: return ApiResult.Err("main.js not found")
        val jsUrl = m.value

        // Get client ID
        val jsResponse = doRequest<String> {
            ImgurClient.client.get(jsUrl)
        }
        val jsData = when (jsResponse) {
            is ApiResult.Ok -> jsResponse.value
            is ApiResult.Err -> return jsResponse
        }
        val mClientId =
            ImgurClient.clientIdRe.find(jsData) ?: return ApiResult.Err("client id not found")
        return ApiResult.Ok(mClientId.groups[1]!!.value)
    }

    private suspend fun callApi(
        id: String, apiType: ImgurApiType
    ): ApiResult<ImmutableList<MediaItem>> {
        val clientId = when (val c = getClientId()) {
            is ApiResult.Ok -> c.value
            is ApiResult.Err -> return c
        }

        val apiResponse = doRequest<ImgurAlbumResponse> {
            ImgurClient.client.get {
                url {
                    protocol = this@Imgur.protocol
                    host = "api.imgur.com"
                    appendPathSegments("post", "v1")
                    when (apiType) {
                        is ImgurApiType.Album -> appendPathSegments("albums")
                        is ImgurApiType.Media -> appendPathSegments("media")
                    }
                    appendPathSegments(id)
                    parameters.append("client_id", clientId)
                    parameters.append("include", "media")
                }
            }
        }

        val parsedResponse = when (apiResponse) {
            is ApiResult.Err -> return apiResponse
            is ApiResult.Ok -> apiResponse.value
        }

        return ApiResult.Ok(parsedResponse.media.map { media ->
            MediaItem(
                mediaType = if (media.mediaType == "video") {
                    MediaType.Video
                } else {
                    MediaType.Image
                },
                url = media.url,
                description = media.metadata.description,
                width = media.width,
                height = media.height
            )
        }.toImmutableList())
    }
}

private sealed class ImgurApiType {
    data object Album : ImgurApiType()
    data object Media : ImgurApiType()
}

private object ImgurClient {
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

    val mainJsRe = Regex("https?://s\\.imgur\\.com/desktop-assets/js/main\\.\\w+\\.js")
    val clientIdRe = Regex("apiClientId: *\"(\\w+)")
}

@Serializable
private class ImgurAlbumResponse(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val media: List<ImgurMedia>,
)

@Serializable
private class ImgurMedia(
    val id: String,
    @SerialName("type") val mediaType: String,
    @SerialName("mime_type") val mimeType: String,
    val name: String,
    val url: String,
    val size: Int,
    val width: Int,
    val height: Int,
    val metadata: ImgurMediaMetadata,
)

@Serializable
private class ImgurMediaMetadata(
    val title: String,
    val description: String,
)