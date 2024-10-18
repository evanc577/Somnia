package dev.evanchang.somnia.ui.settings.screen

import android.util.Log
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
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

    val appSettings by context.dataStore.data.collectAsState(
        initial = AppSettings()
    )
    val clientId = appSettings.apiSettings.redditClientId
    val redirectUri = appSettings.apiSettings.redditRedirectUri

    // Snackbar on login error
    var loginError: String? by remember { mutableStateOf(null) }
    var loginErrorShown by remember { mutableStateOf(false) }
    LaunchedEffect(loginError, loginErrorShown) {
        val loginErrorValue = loginError
        val loginErrorShownValue = loginErrorShown
        if (loginErrorValue == null || loginErrorShownValue) {
            return@LaunchedEffect
        }

        loginError = null
        loginErrorShown = true
        scope.launch {
            snackbarHostState.showSnackbar("Error logging in: $loginErrorValue")
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
            SettingsGroup(title = { Text(text = "Saved accounts") }) {}
        }
        when (val l = loginResult()) {
            is LoginResult.Ok -> Log.d("AccountSettingsScreen", "LoginResult OK")
            is LoginResult.Err -> loginError = l.error

            null -> Unit
        }
    }
}