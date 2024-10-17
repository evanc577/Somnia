package dev.evanchang.somnia.ui.settings

import androidx.annotation.Keep
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
object ApiSettings

fun NavGraphBuilder.settingsNavigation(
    navController: NavController,
) {
    val onNavigateBack: () -> Unit = {
        navController.popBackStack()
    }
    navigation<SettingsNavigation>(startDestination = Settings) {
        settingsDestination(onNavigateBack = onNavigateBack,
            onNavigateToApiSettings = { navController.navigateToApiSettings() })
        apiSettingsDestination(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.settingsDestination(
    onNavigateBack: () -> Unit,
    onNavigateToApiSettings: () -> Unit,
) {
    composable<Settings> {
        SettingsScreen(
            onNavigateBack = onNavigateBack, onNavigateToApiSettings = onNavigateToApiSettings
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