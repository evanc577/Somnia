package dev.evanchang.somnia.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.evanchang.somnia.ui.redditscreen.SubmissionsScaffold

@Composable
fun NavigationScreen(
    navigationViewModel: NavigationViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
) {
    val navigationState = navigationViewModel.navigationUIState.collectAsStateWithLifecycle()
    val navigationBackStack = navigationState.value.navigationBackStack

    Box(
        modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
            val rect = layoutCoordinates.boundsInRoot()
            navigationViewModel.setScreenWidth(rect.topRight.x)
        },
    ) {
        for ((index, entry) in navigationBackStack.withIndex()) {
            when (entry) {
                is NavigationBackStackEntry.SubredditBackStackEntry -> {
                    SubmissionsScaffold(
                        screenStackIndex = index,
                        navigationViewModel = navigationViewModel,
                        submissionsListViewModel = entry.viewModel,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
        }
    }
}