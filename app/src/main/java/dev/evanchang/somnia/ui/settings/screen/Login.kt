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
import androidx.navigation3.runtime.NavBackStack
import dev.evanchang.somnia.api.ApiResult
import dev.evanchang.somnia.api.reddit.RedditLoginApiInstance
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    clientId: String,
    redirectUri: String,
    onLoginFinished: (LoginResult) -> Unit,
    backStack: NavBackStack,
) {
    val scope = rememberCoroutineScope()

    // Declare a string that contains a url
    val state = generateRandomString()
    val endpoint = Uri.Builder().scheme("https").authority("reddit.com").path("api/v1/authorize")
        .appendQueryParameter("client_id", clientId).appendQueryParameter("response_type", "code")
        .appendQueryParameter("state", state).appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("duration", "permanent").appendQueryParameter("scope", "*").build()

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
        backStack = backStack,
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

private suspend fun login(
    clientId: String, code: String, redirectUri: String
): LoginResult {
    // Get access and refresh tokens
    val response = when (val r = RedditLoginApiInstance.api.postAccessToken(
        clientId = clientId,
        redirectUri = redirectUri,
        code = code,
    )) {
        is ApiResult.Ok -> r.value
        is ApiResult.Err -> return LoginResult.Err(r.message)
    }

    // Get account username
    val meResponse = when (val r = RedditLoginApiInstance.api.getApiV1Me(response.accessToken)) {
        is ApiResult.Ok -> r.value
        is ApiResult.Err -> return LoginResult.Err(r.message)
    }

    return LoginResult.Ok(
        user = meResponse.name, accountSettings = AccountSettings(
            clientId = clientId,
            refreshToken = response.refreshToken,
            redirectUri = redirectUri,
        )
    )
}