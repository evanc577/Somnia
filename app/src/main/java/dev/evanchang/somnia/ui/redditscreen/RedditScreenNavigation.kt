package dev.evanchang.somnia.ui.redditscreen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.evanchang.somnia.ui.navigation.AppScreen
import dev.evanchang.somnia.ui.navigation.NavigationScreen

fun NavGraphBuilder.homeDestination(
    onNavigateToSettings: () -> Unit,
) {
    composable<AppScreen.SubredditScreen> {
        NavigationScreen(onNavigateToSettings = onNavigateToSettings)
    }
}