package dev.evanchang.somnia.ui.settings.screen

import android.content.Context
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.api.RedditHttpClient
import dev.evanchang.somnia.api.reddit.RedditApiInstance
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (clientId: String, redirectUri: String) -> Unit,
    loginResult: () -> LoginResult?,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Extract API settings
    val appSettings by context.dataStore.data.collectAsState(
        initial = AppSettings()
    )
    val clientId = appSettings.apiSettings.redditClientId
    val redirectUri = appSettings.apiSettings.redditRedirectUri

    // Set to true after action is taken after login success or error
    var loginFinished by remember { mutableStateOf(false) }
    if (!loginFinished) {
        loginFinished = true
        when (val loginResultValue = loginResult()) {
            is LoginResult.Ok -> scope.launch {
                addAccountSettings(context, loginResultValue.user, loginResultValue.accountSettings)
            }

            is LoginResult.Err -> scope.launch {
                snackbarHostState.showSnackbar("Error logging in: ${loginResultValue.error}")
            }

            null -> Unit
        }
    }

    SettingsScaffold(
        title = "Account Settings",
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        onNavigateBack = onNavigateBack,
    ) {
        SettingsMenuLink(
            title = { Text(text = "Add new account") },
            onClick = {
                if (clientId == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar("No Reddit client ID set")
                    }
                } else {
                    onNavigateToLogin(clientId, redirectUri)
                }
            },
        )
        if (appSettings.accountSettings.isNotEmpty()) {
            SettingsGroup(title = { Text(text = "Saved accounts") }) {
                for (account in appSettings.accountSettings) {
                    Text(text = account.key)
                }
            }
        }
    }
}

private suspend fun addAccountSettings(
    context: Context,
    user: String,
    accountSettings: AccountSettings
) {
    context.dataStore.updateData { appSettings ->
        appSettings.copy(
            activeUser = user,
            accountSettings = appSettings.accountSettings.mutate { accountSettingsMap ->
                accountSettingsMap[user] = accountSettings
            }
        )
    }
}