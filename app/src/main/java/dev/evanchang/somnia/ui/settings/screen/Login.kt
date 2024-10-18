package dev.evanchang.somnia.ui.settings.screen

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcelable
import android.view.ViewGroup
import android.webkit.WebResourceRequest
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
import androidx.datastore.core.Serializer
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

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

    // Adding a WebView inside AndroidView with layout as full screen
    SettingsScaffold(
        title = "Log in",
        onNavigateBack = onNavigateBack,
    ) {
        if (!retrievingAccessToken) {
            AndroidView(factory = {
                // Clear all webview data to force new login
//                WebStorage.getInstance().deleteAllData()
//                CookieManager.getInstance().removeAllCookies(null)

                WebView(it).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = CustomWebViewClient(
                        redirectUri = redirectUri,
                        state = state,
                        onAuthorization = onAuthorization,
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
    class Ok(val accountSettings: AccountSettings) : LoginResult()
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
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request == null) {
            return false
        }

        if (!request.url.toString().startsWith(redirectUri)) {
            return false
        }

        val queryState = request.url.getQueryParameter("state")
        if (queryState != state) {
            return false
        }

        val queryCode = request.url.getQueryParameter("code")
        if (queryCode == null) {
            return false
        }

        onAuthorization(queryCode)
        return true
    }
}

private suspend fun login(
    clientId: String,
    code: String,
    redirectUri: String
): LoginResult {
    delay(5000)
    return LoginResult.Err("TODO login!")
}
