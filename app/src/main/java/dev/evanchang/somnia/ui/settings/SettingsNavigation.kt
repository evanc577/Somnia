package dev.evanchang.somnia.ui.settings

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.evanchang.somnia.ui.settings.screen.LoginWebView
import dev.evanchang.somnia.ui.settings.screen.AccountSettingsScreen
import dev.evanchang.somnia.ui.settings.screen.ApiSettingsScreen
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
            onNavigateToLogin = { clientId ->
                navController.navigateToLogin(clientId = clientId)
            })
        loginDestination(onNavigateBack = onNavigateBack)
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
    onNavigateToLogin: (clientId: String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<AccountSettings> {
        AccountSettingsScreen(
            onNavigateBack = onNavigateBack, onNavigateToLogin = onNavigateToLogin
        )
    }
}

fun NavController.navigateToAccountSettings() {
    navigate(AccountSettings)
}

fun NavGraphBuilder.loginDestination(
    onNavigateBack: () -> Unit,
) {
    composable<Login> {
        val route: Login = it.toRoute()
        LoginWebView(clientId = route.clientId, onNavigateBack = onNavigateBack)
    }
}

fun NavController.navigateToLogin(clientId: String) {
    val x = Login(clientId = clientId)
    navigate(x)
}