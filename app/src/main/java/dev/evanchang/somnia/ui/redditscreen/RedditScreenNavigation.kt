package dev.evanchang.somnia.ui.redditscreen

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Keep
@Serializable
object Home

fun NavGraphBuilder.homeDestination(
    onNavigateToSettings: () -> Unit,
) {
    composable<Home> {
        HomeScreen(onNavigateToSettings = onNavigateToSettings)
    }
}