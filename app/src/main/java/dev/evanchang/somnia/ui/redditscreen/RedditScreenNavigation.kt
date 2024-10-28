package dev.evanchang.somnia.ui.redditscreen

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.evanchang.somnia.appSettings.AppSettings
import dev.evanchang.somnia.ui.navigation.NavigationScreen
import kotlinx.serialization.Serializable

@Keep
@Serializable
object RedditNav

fun NavGraphBuilder.homeDestination(
    appSettings: AppSettings,
    onNavigateToSettings: () -> Unit,
) {
    composable<RedditNav> {
        NavigationScreen(appSettings = appSettings, onNavigateToSettings = onNavigateToSettings)
    }
}