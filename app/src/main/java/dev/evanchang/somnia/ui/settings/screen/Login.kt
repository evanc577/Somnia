package dev.evanchang.somnia.ui.settings.screen

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    clientId: String,
    onNavigateBack: () -> Unit,
) {
    // Declare a string that contains a url
    val state = generateRandomString()
    val redirect = "127.0.0.1"
    val endpoint = Uri.Builder().scheme("https").authority("reddit.com").path("api/v1/authorize")
        .appendQueryParameter("client_id", clientId).appendQueryParameter("response_type", "code")
        .appendQueryParameter("state", state)
        .appendQueryParameter("redirect_uri", "http://${redirect}")
        .appendQueryParameter("duration", "permanent").appendQueryParameter("scope", "*").build()

    // Adding a WebView inside AndroidView with layout as full screen
    SettingsScaffold(
        title = "Log in",
        onNavigateBack = onNavigateBack,
    ) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = CustomWebViewClient(
                    authority = redirect, state = state, onNavigateBack = onNavigateBack
                )
                settings.javaScriptEnabled = true
            }
        }, update = {
            it.loadUrl(endpoint.toString())
        })
    }
}

class CustomWebViewClient(
    private val authority: String,
    private val state: String,
    private val onNavigateBack: () -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request == null) {
            return false
        }

        if (request.url.authority != authority) {
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

        onNavigateBack()
        return true
    }
}

private fun generateRandomString(): String {
    val length = 32
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { allowedChars.random() }.joinToString("")
}