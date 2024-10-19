package dev.evanchang.somnia.ui.settings.screen

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcelable
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.api.reddit.RedditAuthApiInstance
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    clientId: String,
    redirectUri: String,
    onNavigateBack: () -> Unit,
    onLoginFinished: (LoginResult) -> Unit,
) {
    val scope = rememberCoroutineScope()

    // Declare a string that contains a url
    val state = generateRandomString()
    val endpoint = Uri.Builder()
        .scheme("https")
        .authority("reddit.com")
        .path("api/v1/authorize")
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("state", state)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("duration", "permanent")
        .appendQueryParameter("scope", "*")
        .build()

    // Login return values
    var retrievingAccessToken by remember { mutableStateOf(false) }
    val onAuthorization: (code: String) -> Unit = { code ->
        scope.launch {
            retrievingAccessToken = true
            val loginResult = login(
                clientId = clientId,
                code = code,
                redirectUri = redirectUri,
            )
            onLoginFinished(loginResult)
        }
    }
    val onAuthorizationError: (error: String) -> Unit = { error ->
        val loginResult = LoginResult.Err(error = error)
        onLoginFinished(loginResult)
    }

    // Adding a WebView inside AndroidView with layout as full screen
    SettingsScaffold(
        title = "Log in",
        onNavigateBack = onNavigateBack,
    ) {
        if (!retrievingAccessToken) {
            AndroidView(factory = {
                // Clear all webview data to force new login
                WebStorage.getInstance().deleteAllData()
                CookieManager.getInstance().removeAllCookies(null)

                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = CustomWebViewClient(
                        redirectUri = redirectUri,
                        state = state,
                        onAuthorization = onAuthorization,
                        onAuthorizationError = onAuthorizationError,
                    )
                    settings.javaScriptEnabled = true
                }
            }, update = {
                it.loadUrl(endpoint.toString())
            })
        } else {
            Text(text = "Logging in...")
        }
    }
}

@Keep
@Parcelize
sealed class LoginResult : Parcelable {
    class Ok(val user: String, val accountSettings: AccountSettings) : LoginResult()
    class Err(val error: String) : LoginResult()
}

private fun generateRandomString(): String {
    val length = 32
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}

class CustomWebViewClient(
    private val redirectUri: String,
    private val state: String,
    private val onAuthorization: (code: String) -> Unit,
    private val onAuthorizationError: (error: String) -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request == null) {
            return false
        }

        // Verify URL starts with redirect uri
        if (!request.url.toString().startsWith(redirectUri)) {
            return false
        }

        // Check state
        val queryState = request.url.getQueryParameter("state")
        if (queryState != state) {
            onAuthorizationError("Invalid state")
            return true
        }

        // Check error message
        val queryError = request.url.getQueryParameter("error")
        if (queryError != null) {
            val error = when (queryError) {
                "access_denied" -> "Access denied"
                "unsupported_response_type" -> "Unsupported response type"
                "invalid_scope" -> "Invalid scope"
                "invalid_request" -> "Invalid request"
                else -> queryError
            }
            onAuthorizationError(error)
            return true
        }

        // Extract code
        val queryCode = request.url.getQueryParameter("code")
        if (queryCode == null) {
            onAuthorizationError("No code")
            return true
        }

        // Perform log in
        onAuthorization(queryCode)
        return true
    }
}

@OptIn(ExperimentalEncodingApi::class)
private suspend fun login(
    clientId: String,
    code: String,
    redirectUri: String
): LoginResult {
    val authorization = "basic ${Base64.encode("${clientId}:".encodeToByteArray())}"
    val accessTokenResponse = try {
        RedditAuthApiInstance.api.postAccessToken(
            authorization = authorization,
            grantType = "authorization_code",
            code = code,
            redirectUri = redirectUri,
        )
    } catch (e: Exception) {
        return LoginResult.Err("access_token ${e.toString()}")
    }
    if (!accessTokenResponse.isSuccessful) {
        return LoginResult.Err("access_token status code: ${accessTokenResponse.code()}")
    }
    val accessTokenBody = accessTokenResponse.body()
    if (accessTokenBody == null) {
        return LoginResult.Err("access_token no response body")
    }
    val accountSettings = AccountSettings(
        clientId = clientId,
        refreshToken = accessTokenBody.refreshToken,
        bearerToken = accessTokenBody.accessToken,
        redirectUri = redirectUri,
    )

    // Get username
    val meResponse = try {
        RedditApiInstance.api.getApiV1Me(authorization = "bearer ${accountSettings.bearerToken}")
    } catch (e: Exception) {
        return LoginResult.Err("me ${e.toString()}")
    }
    val meBody = meResponse.body()
    if (meBody == null) {
        return LoginResult.Err("me no response body")
    }
    val user = meBody.name

    return LoginResult.Ok(user = user, accountSettings = accountSettings)
}
