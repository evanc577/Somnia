package dev.evanchang.somnia.ui.redditscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import dev.evanchang.somnia.ui.submissions.SubmissionsList
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModelFactory
import kotlin.math.roundToInt

private val BOTTOM_BAR_HEIGHT = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsScaffold(
    submissionsListViewModel: SubmissionsListViewModel = viewModel(
        factory = SubmissionsListViewModelFactory("dreamcatcher")
    ),
    onNavigateToSettings: () -> Unit,
) {
    val density = LocalDensity.current

    // Scrolling
    val listState = rememberLazyStaggeredGridState()
    var scrollToTop by remember { mutableStateOf(false) }
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            listState.scrollToItem(0)
            scrollToTop = false
        }
    }

    // Top bar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var topPadding by remember { mutableStateOf(0.dp) }
    val statusBarHeightPx = WindowInsets.safeDrawing.getTop(density)

    // Bottom bar
    val navBarHeightPx = WindowInsets.safeDrawing.getBottom(density)
    val bottomBarHeight = with(density) { navBarHeightPx.toDp() + BOTTOM_BAR_HEIGHT }
    val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx().toFloat() }
    var tmpBottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(tmpBottomBarOffsetHeightPx) {
        bottomBarOffsetHeightPx = tmpBottomBarOffsetHeightPx.coerceIn(-(bottomBarHeightPx), 0f)
        tmpBottomBarOffsetHeightPx = bottomBarOffsetHeightPx
    }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                tmpBottomBarOffsetHeightPx = (bottomBarOffsetHeightPx + available.y)
                return Offset.Zero
            }
        }
    }

    // Buttons
    var menuExpanded by remember { mutableStateOf(false) }

    // Update sort
    var updateSort: String? by remember { mutableStateOf(null) }
    val lazyPagingItems = submissionsListViewModel.submissions.collectAsLazyPagingItems()
    LaunchedEffect(updateSort) {
        val updateSortVal = updateSort ?: return@LaunchedEffect
        submissionsListViewModel.updateSort(updateSortVal)
        submissionsListViewModel.updateIsRefreshing(true)
        lazyPagingItems.refresh()
    }

    // UI
    Scaffold(modifier = Modifier
        .nestedScroll(nestedScrollConnection)
        .nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(title = {
            Text(text = "Somnia")
        },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            actions = {
                IconButton(onClick = {
                    updateSort = "hot"
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "")
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
                    }
                    Menu(expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        navigateToSettings = { onNavigateToSettings() })
                }
            },
            modifier = Modifier.clickable { scrollToTop = true })
    }, bottomBar = {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .height(bottomBarHeight)
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.roundToInt()) }
        ) {
            Box(
                contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BOTTOM_BAR_HEIGHT)
                ) {
                    for (i in 0..4) {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "")
                        }
                    }
                }
            }
        }
    }) { padding ->
        // Only use top bar padding without status bar
        topPadding =
            (padding.calculateTopPadding() - with(density) { statusBarHeightPx.toDp() }).coerceAtLeast(
                0.dp
            )
        Column {
            Spacer(modifier = Modifier.height(with(density) { statusBarHeightPx.toDp() }))
            SubmissionsList(
                submissionsListViewModel = submissionsListViewModel,
                listState = listState,
                topPadding = topPadding
            )
        }
    }
}

@Composable
private fun Menu(expanded: Boolean, onDismissRequest: () -> Unit, navigateToSettings: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceBright,
    ) {
        DropdownMenuItem(onClick = {
            navigateToSettings()
            onDismissRequest()
        },
            text = { Text("Settings") },
            leadingIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "") })
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    SubmissionsScaffold(onNavigateToSettings = {})
}