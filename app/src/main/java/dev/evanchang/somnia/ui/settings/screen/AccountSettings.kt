package dev.evanchang.somnia.ui.settings.screen

import android.content.Context
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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import dev.evanchang.somnia.appSettings.AccountSettings
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.settings.composable.AccountItem
import dev.evanchang.somnia.ui.settings.composable.SettingsScaffold
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun AccountSettingsScreen(
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Extract API settings
    val settings by context.dataStore.data.collectAsState(initial = null)
    val appSettings = remember(settings) { settings }

    // Set to true after action is taken after login success or error
    var loginResult: LoginResult? by remember { mutableStateOf(null) }
    LaunchedEffect(loginResult) {
        val loginResult = loginResult
        if (loginResult != null) {
            when (loginResult) {
                is LoginResult.Ok -> scope.launch {
                    addAccountSettings(
                        context, loginResult.user, loginResult.accountSettings
                    )
                }

                is LoginResult.Err -> scope.launch {
                    snackbarHostState.showSnackbar("Error logging in: ${loginResult.error}")
                }
            }
        }
    }

    SettingsScaffold(
        title = "Account Settings",
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        onBack = onBack,
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
                        onNavigate(
                            Nav.Settings(
                                SettingsNavKey.Account(
                                    AccountSettingsNavKey.Login(
                                        clientId = appSettings.apiSettings.redditClientId,
                                        redirectUri = appSettings.apiSettings.redditRedirectUri,
                                        onLoginFinished = { result ->
                                            loginResult = result
                                            onBack(1)
                                        },
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
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
): NavEntry<NavKey> {
    return when (key) {
        is AccountSettingsNavKey.TopLevel -> NavEntry(key) {
            AccountSettingsScreen(
                onBack = onBack,
                onNavigate = onNavigate,
            )
        }

        is AccountSettingsNavKey.Login -> NavEntry(key) {
            LoginScreen(
                clientId = key.clientId,
                redirectUri = key.redirectUri,
                onLoginFinished = key.onLoginFinished,
                onBack = onBack,
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
        val onLoginFinished: (LoginResult) -> Unit,
    ) : AccountSettingsNavKey()
}

private suspend fun addAccountSettings(
    context: Context, user: String, accountSettings: AccountSettings
) {
    context.dataStore.updateData { appSettings ->
        appSettings.copy(
            activeUser = user,
            accountSettings = appSettings.accountSettings.mutate { accountSettingsMap ->
                accountSettingsMap[user] = accountSettings
            },
        )
    }
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
