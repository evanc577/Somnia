package dev.evanchang.somnia.ui.redditscreen

import androidx.annotation.Keep
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.evanchang.somnia.ui.navigation.NavigationScreen
import kotlinx.serialization.Serializable

@Keep
@Serializable
object RedditNav

fun NavGraphBuilder.homeDestination(
    onNavigateToSettings: () -> Unit,
) {
    composable<RedditNav> {
        NavigationScreen(onNavigateToSettings = onNavigateToSettings)
    }
}