package dev.evanchang.somnia.ui.settings.screen

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit, onNavigateToLogin: (clientId: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val appSettings by context.dataStore.data.collectAsState(
        initial = AppSettings()
    )
    val clientId = appSettings.apiSettings.redditApiClientId

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
                    onNavigateToLogin(clientId)
                }
            },
        )
        if (appSettings.accountSettings.isNotEmpty()) {
            SettingsGroup(title = { Text(text = "Saved accounts") }) {

            }
        }
    }
}