package dev.evanchang.somnia.ui.settings.screen

import android.content.Context
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.navigation.LocalNavigation
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.settings.composable.AccountItem
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun AccountSettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val nav = LocalNavigation.current

    // Extract API settings
    val settings by context.dataStore.data.collectAsState(initial = null)
    val appSettings = remember(settings) { settings }

    SettingsScaffold(
        title = "Account Settings",
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        if (appSettings != null) {
            SettingsMenuLink(
                title = { Text(text = "Add new account") },
                onClick = {
                    if (appSettings.apiSettings.redditClientId == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Client ID not set")
                        }
                    } else {
                        nav.onNavigate(
                            Nav.Settings(
                                SettingsNavKey.Account(
                                    AccountSettingsNavKey.Login(
                                        clientId = appSettings.apiSettings.redditClientId,
                                        redirectUri = appSettings.apiSettings.redditRedirectUri,
                                    )
                                )
                            )
                        )
                    }
                },
            )
            if (appSettings.accountSettings.isNotEmpty()) {
                SettingsGroup(title = { Text(text = "Saved accounts") }) {
                    val activeUser = appSettings.activeUser
                    AccountItem(
                        username = null, accountSettings = null,
                        isActiveUser = activeUser == null,
                        onSetActiveUser = {
                            scope.launch {
                                setActiveUser(context, null)
                            }
                        },
                        onDeleteUser = {},
                    )
                    for (account in appSettings.accountSettings) {
                        AccountItem(
                            username = account.key,
                            accountSettings = account.value,
                            isActiveUser = activeUser == account.key,
                            onSetActiveUser = {
                                scope.launch {
                                    setActiveUser(context = context, user = account.key)
                                }
                            },
                            onDeleteUser = {
                                scope.launch {
                                    deleteUser(context = context, user = account.key)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

fun AccountSettingsNav(
    key: AccountSettingsNavKey,
): NavEntry<NavKey> {
    return when (key) {
        is AccountSettingsNavKey.TopLevel -> NavEntry(key) {
            AccountSettingsScreen()
        }

        is AccountSettingsNavKey.Login -> NavEntry(key) {
            LoginScreen(
                clientId = key.clientId,
                redirectUri = key.redirectUri,
            )
        }
    }
}

@Serializable
sealed class AccountSettingsNavKey : NavKey {
    @Serializable
    data object TopLevel : AccountSettingsNavKey()

    @Serializable
    data class Login(
        val clientId: String,
        val redirectUri: String,
    ) : AccountSettingsNavKey()
}


private suspend fun setActiveUser(
    context: Context, user: String?,
) {
    context.dataStore.updateData { appSettings ->
        appSettings.copy(activeUser = user)
    }
}

private suspend fun deleteUser(
    context: Context, user: String,
) {
    context.dataStore.updateData { appSettings ->
        appSettings.copy(
            activeUser = null,
            accountSettings = appSettings.accountSettings.mutate { accountSettingsMap ->
                accountSettingsMap.remove(user)
            },
        )
    }
}
