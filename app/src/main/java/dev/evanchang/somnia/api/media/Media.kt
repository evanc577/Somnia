package dev.evanchang.somnia.api.media

import dev.evanchang.somnia.api.ApiResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import okhttp3.internal.immutableListOf

@Serializable
sealed class Media : MediaResolver {
    @Serializable
    class RedditGallery(val images: ImmutableList<String>) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return ApiResult.Ok(images.map {
                MediaItem(
                    mediaType = MediaType.Image,
                    url = it,
                )
            }.toImmutableList())
        }
    }

    @Serializable
    class RedditVideo(val video: String) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return ApiResult.Ok(
                immutableListOf(
                    MediaItem(
                        mediaType = MediaType.Video,
                        url = video,
                    )
                ).toImmutableList()
            )
        }
    }

    @Serializable
    class Streamable(val id: String) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return when (val response = StreamableApi().getVideo(id)) {
                is ApiResult.Err -> response

                is ApiResult.Ok -> ApiResult.Ok(listOf(response.value).toImmutableList())
            }
        }
    }

    @Serializable
    class ImgurAlbum(val id: String) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return Imgur().getAlbum(id)
        }
    }

    @Serializable
    class ImgurMedia(val id: String) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return Imgur().getMedia(id)
        }
    }

    @Serializable
    class Redgifs(val id: String) : Media() {
        override suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>> {
            return ApiResult.Ok(
                listOf(
                    MediaItem(
                        mediaType = MediaType.Video,
                        url = "https://api.redgifs.com/v2/gifs/${id}/hd.m3u8",
                    )
                ).toImmutableList()
            )
        }
    }
}

interface MediaResolver {
    suspend fun fetchData(): ApiResult<ImmutableList<MediaItem>>
}

enum class MediaType {
    Image, Video,
}

data class MediaItem(
    val mediaType: MediaType,
    val url: String,
    val description: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

private object MediaRegexManager {
    val streamableRe = Regex("https?://(?:\\w+\\.)?streamable\\.com/(\\w+)")
    val imgurAlbumRe = Regex("https?://imgur\\.com\\/a\\/[^/]*?(\\w+)(?:\$|/)")
    val imgurMediaRe = Regex("https?://(?:i\\.)?imgur\\.com/[^/]*?(\\w+)(?:\$|/|\\.\\w+)")
    val redgifsRe = Regex("https?://(?:\\w+\\.)?redgifs\\.com/watch/(\\w+)")
}

fun parseMediaFromUrl(url: String): Media? {
    // Streamable
    run {
        val m = MediaRegexManager.streamableRe.find(url)
        val id = m?.groups?.get(1)?.value
        if (id != null) {
            return Media.Streamable(id)
        }
    }

    // Imgur album
    run {
        val m = MediaRegexManager.imgurAlbumRe.find(url)
        val id = m?.groups?.get(1)?.value
        if (id != null) {
            return Media.ImgurAlbum(id)
        }
    }

    // Imgur media
    run {
        val m = MediaRegexManager.imgurMediaRe.find(url)
        val id = m?.groups?.get(1)?.value
        if (id != null) {
            return Media.ImgurMedia(id)
        }
    }

    // Redgifs
    run {
        val m = MediaRegexManager.redgifsRe.find(url)
        val id = m?.groups?.get(1)?.value
        if (id != null) {
            return Media.Redgifs(id)
        }
    }

    return null
}