package dev.evanchang.somnia.api

import android.util.Log
import dev.evanchang.somnia.api.reddit.RedditLoginApiInstance
import dev.evanchang.somnia.appSettings.AccountSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object UnauthenticatedHttpClient {
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
            level = LogLevel.HEADERS
        }
    }
}


// Create custom replacement for Ktor bearer auth plugin that supports multiple status codes to retry
// on other than just 401
class BearerAuthPluginConfig {
    var loadTokens: suspend () -> BearerTokens? = { null }
    var refreshTokens: suspend () -> BearerTokens? = { null }
}

val BearerAuthPlugin = createClientPlugin("CustomHeaderPlugin", ::BearerAuthPluginConfig) {
    val loadTokens = pluginConfig.loadTokens
    val refreshTokens = pluginConfig.refreshTokens

    onRequest { request, _ ->
        var tokens = loadTokens()
        if (tokens == null) {
            tokens = refreshTokens()
        }
        if (tokens != null) {
            request.headers.append("authorization", "bearer ${tokens.accessToken}")
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
    private var account: AccountSettings? = null
    private var bearerToken: String? = null

    fun login(accountSettings: AccountSettings) {
        this.account = accountSettings
    }

    fun logout() {
        this.account = null
        this.bearerToken = null
    }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            })
        }
        install(BearerAuthPlugin) {
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
            level = LogLevel.HEADERS
        }
    }
}