package dev.evanchang.somnia.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.evanchang.somnia.ui.redditscreen.SubmissionsScaffold
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel

@Composable
fun NavigationScreen(
    navigationViewModel: NavigationViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
) {
    val navigationState = navigationViewModel.navigationUIState.collectAsStateWithLifecycle()
    val navigationBackStack = navigationState.value.navigationBackStack
    val screenXOffsetSet = remember { mutableStateOf(false) }

    Box(modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        //--Set screenXOffset if not set--
        if (!screenXOffsetSet.value) {
            val rect = layoutCoordinates.boundsInRoot()
            navigationViewModel.setScreenWidth(rect.topRight.x)
            screenXOffsetSet.value = true
        }
    }) {
        for ((index, entry) in navigationBackStack.withIndex()) {
            when (entry.screen) {
                is AppScreen.SubredditScreen -> {
                    AnimatedVisibility(visible = index >= navigationBackStack.size - 2) {
                        SubmissionsScaffold(
                            screenStackIndex = index,
                            navigationViewModel = navigationViewModel,
                            submissionsListViewModel = entry.viewModel as SubmissionsListViewModel,
                            onNavigateToSettings = onNavigateToSettings
                        )
                    }
                }

                is AppScreen.CommentScreen -> {}
            }
        }
    }
}