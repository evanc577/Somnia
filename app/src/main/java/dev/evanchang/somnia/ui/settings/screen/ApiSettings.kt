package dev.evanchang.somnia.ui.settings.screen

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.compose.settings.ui.SettingsGroup
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import dev.evanchang.somnia.ui.settings.composable.SettingsTextEdit
import kotlinx.coroutines.launch

@Composable
fun ApiSettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val settings by context.dataStore.data.collectAsState(initial = null)
    val apiSettings = remember(settings) { settings?.apiSettings }

    SettingsScaffold(title = "API Settings") {
        if (apiSettings != null) {
            SettingsGroup(title = { Text(text = "Reddit") }) {
                SettingsTextEdit(
                    title = "Reddit Client ID",
                    subtitle = {
                        if (apiSettings.redditClientId == null) {
                            Text(text = "(Unset)", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = apiSettings.redditClientId)
                        }
                    },
                    description = "Reddit developed app client ID.",
                    defaultText = apiSettings.redditClientId ?: "",
                    onConfirmRequest = {
                        coroutineScope.launch {
                            setRedditApiClientId(context, it)
                        }
                    })

                SettingsTextEdit(
                    title = "Reddit redirect URI",
                    subtitle = { Text(text = apiSettings.redditRedirectUri) },
                    description = "Reddit developed app redirect URI.",
                    defaultText = apiSettings.redditRedirectUri,
                    onConfirmRequest = {
                        coroutineScope.launch {
                            setRedditApiRedirectUri(context, it)
                        }
                    })

                SettingsTextEdit(
                    title = "Reddit User-Agent",
                    subtitle = { Text(text = apiSettings.redditUserAgent) },
                    description = "User-Agent header sent with all Reddit API requests. Leave empty to return to default User-Agent.",
                    defaultText = apiSettings.redditUserAgent,
                    onConfirmRequest = {
                        coroutineScope.launch {
                            setRedditApiUserAgent(context, it)
                        }
                    })
            }
        }
    }
}

private suspend fun setRedditApiClientId(context: Context, clientId: String) {
    val x = clientId.ifEmpty {
        null
    }
    context.dataStore.updateData {
        it.copy(
            apiSettings = it.apiSettings.copy(
                redditClientId = x
            )
        )
    }
}

private suspend fun setRedditApiRedirectUri(context: Context, redirect: String) {
    val x = redirect.ifEmpty {
        AppSettings().apiSettings.redditRedirectUri
    }
    context.dataStore.updateData {
        it.copy(
            apiSettings = it.apiSettings.copy(
                redditRedirectUri = x
            )
        )
    }
}

private suspend fun setRedditApiUserAgent(context: Context, userAgent: String) {
    val x = userAgent.ifEmpty {
        AppSettings().apiSettings.redditUserAgent
    }
    context.dataStore.updateData {
        it.copy(
            apiSettings = it.apiSettings.copy(
                redditUserAgent = x
            )
        )
    }
}

@Preview
@Composable
private fun ApiSettingsScreenPreview() {
    ApiSettingsScreen()
}