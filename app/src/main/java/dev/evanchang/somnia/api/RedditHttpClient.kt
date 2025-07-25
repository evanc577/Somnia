package dev.evanchang.somnia.api

import android.util.Log
import dev.evanchang.somnia.api.reddit.RedditLoginApiInstance
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.data.MediaMetadata
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiationConfig
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

private val jsonSerialization: ContentNegotiationConfig.() -> Unit = {
    json(Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        serializersModule = SerializersModule {
            polymorphicDefaultDeserializer(MediaMetadata::class) {
                MediaMetadata.None.serializer()
            }
        }
    })
}

object UnauthenticatedHttpClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation, jsonSerialization)
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

// Create custom replacement for Ktor bearer auth plugin that supports multiple status codes to retry
// on other than just 401
class BearerAuthPluginConfig {
    var getUserAgent: suspend () -> String? = { null }
    var loadTokens: suspend () -> BearerTokens? = { null }
    var refreshTokens: suspend () -> BearerTokens? = { null }
}

val BearerAuthPlugin = createClientPlugin("CustomHeaderPlugin", ::BearerAuthPluginConfig) {
    val getUserAgent = pluginConfig.getUserAgent
    val loadTokens = pluginConfig.loadTokens
    val refreshTokens = pluginConfig.refreshTokens

    onRequest { request, _ ->
        // Add auth tokens
        var tokens = loadTokens()
        if (tokens == null) {
            tokens = refreshTokens()
        }
        if (tokens != null) {
            request.headers.append("authorization", "bearer ${tokens.accessToken}")
        }

        // Add user-agent
        val userAgent = getUserAgent()
        if (userAgent != null) {
            request.headers.append("user-agent", userAgent)
        }
    }
    on(Send) { request ->
        val retryCodes = setOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden)
        val originalCall = proceed(request)
        originalCall.response.run {
            if (!retryCodes.contains(status)) {
                return@run originalCall
            }
            val tokens = refreshTokens() ?: return@run originalCall
            request.headers["authorization"] = "bearer ${tokens.accessToken}"
            proceed(request)
        }
    }
}

object RedditHttpClient {
    private var userAgent: String? = null
    private var account: AccountSettings? = null
    private var bearerToken: String? = null

    fun login(accountSettings: AccountSettings) {
        this.account = accountSettings
        this.bearerToken = null
    }

    fun logout() {
        this.account = null
        this.bearerToken = null
    }

    fun setUserAgent(ua: String?) {
        userAgent = ua
    }

    val client = HttpClient(CIO) {

        install(ContentNegotiation, jsonSerialization)
        install(BearerAuthPlugin) {
            getUserAgent = { userAgent }
            loadTokens = {
                val accountVal = account
                val bearerTokenVal = bearerToken
                if (accountVal == null || bearerTokenVal == null) {
                    null
                } else {
                    BearerTokens(
                        accessToken = bearerTokenVal,
                        refreshToken = accountVal.refreshToken,
                    )
                }
            }
            refreshTokens = refreshTokens@{
                val accountVal = account ?: return@refreshTokens null

                // Call API for new access token
                val response = when (val r = RedditLoginApiInstance.api.postRefreshAccessToken(
                    clientId = accountVal.clientId,
                    redirectUri = accountVal.redirectUri,
                    refreshToken = accountVal.refreshToken,
                )) {
                    is ApiResult.Ok -> r.value
                    is ApiResult.Err -> return@refreshTokens null
                }

                bearerToken = response.accessToken
                BearerTokens(
                    accessToken = response.accessToken, refreshToken = response.refreshToken
                )
            }
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