package dev.evanchang.somnia.ui.settings

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.evanchang.somnia.ui.settings.screen.AccountSettingsScreen
import dev.evanchang.somnia.ui.settings.screen.ApiSettingsScreen
import dev.evanchang.somnia.ui.settings.screen.LoginResult
import dev.evanchang.somnia.ui.settings.screen.LoginWebView
import dev.evanchang.somnia.ui.settings.screen.SettingsScreen
import kotlinx.serialization.Serializable

@Keep
@Serializable
object SettingsNavigation

@Keep
@Serializable
object Settings

@Keep
@Serializable
object AccountSettings

@Keep
@Serializable
data class Login(
    val clientId: String,
    val redirectUri: String,
)

@Keep
@Serializable
object ApiSettings

fun NavGraphBuilder.settingsNavigation(
    navController: NavController,
) {
    val onNavigateBack: () -> Unit = {
        navController.popBackStack()
    }
    navigation<SettingsNavigation>(startDestination = Settings) {
        settingsDestination(onNavigateBack = onNavigateBack,
            onNavigateToApiSettings = { navController.navigateToApiSettings() },
            onNavigateToAccountSettings = { navController.navigateToAccountSettings() })
        accountSettingsDestination(
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = { clientId, redirectUri ->
                navController.navigateToLogin(
                    clientId = clientId,
                    redirectUri = redirectUri,
                )
            })
        loginDestination(onNavigateBack = {
            navController.previousBackStackEntry?.savedStateHandle?.set("loginResult", null)
            onNavigateBack()
        }, onLoginFinished = { loginResult ->
            navController.previousBackStackEntry?.savedStateHandle?.set("loginResult", loginResult)
            onNavigateBack()
        })
        apiSettingsDestination(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.settingsDestination(
    onNavigateBack: () -> Unit,
    onNavigateToApiSettings: () -> Unit,
    onNavigateToAccountSettings: () -> Unit,
) {
    composable<Settings> {
        SettingsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToApiSettings = onNavigateToApiSettings,
            onNavigateToAccountSettings = onNavigateToAccountSettings,
        )
    }
}

fun NavController.navigateToSettings() {
    navigate(Settings)
}

fun NavGraphBuilder.apiSettingsDestination(
    onNavigateBack: () -> Unit,
) {
    composable<ApiSettings> {
        ApiSettingsScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavController.navigateToApiSettings() {
    navigate(ApiSettings)
}

fun NavGraphBuilder.accountSettingsDestination(
    onNavigateToLogin: (clientId: String, redirectUri: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AccountSettings> {
        AccountSettingsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLogin = onNavigateToLogin,
            loginResult = {
                return@AccountSettingsScreen it.savedStateHandle.get("loginResult")
            }
        )
    }
}

fun NavController.navigateToAccountSettings() {
    navigate(AccountSettings)
}

fun NavGraphBuilder.loginDestination(
    onNavigateBack: () -> Unit,
    onLoginFinished: (LoginResult) -> Unit,
) {
    composable<Login> {
        val route: Login = it.toRoute()
        LoginWebView(
            clientId = route.clientId,
            redirectUri = route.redirectUri,
            onNavigateBack = onNavigateBack,
            onLoginFinished = onLoginFinished,
        )
    }
}

fun NavController.navigateToLogin(clientId: String, redirectUri: String) {
    val x = Login(clientId = clientId, redirectUri = redirectUri)
    navigate(x)
}