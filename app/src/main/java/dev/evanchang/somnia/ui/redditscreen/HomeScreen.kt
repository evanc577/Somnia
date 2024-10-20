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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.scale
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
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.ui.submissions.SubmissionsList
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModel
import dev.evanchang.somnia.ui.submissions.SubmissionsListViewModelFactory
import dev.evanchang.somnia.ui.util.BottomSheetGridItem
import dev.evanchang.somnia.ui.util.BottomSheetItem
import kotlin.math.roundToInt

private val BOTTOM_BAR_HEIGHT = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionsScaffold(
    submissionsListViewModel: SubmissionsListViewModel = viewModel(
        factory = SubmissionsListViewModelFactory(
            subreddit = "dreamcatcher", sort = SubmissionSort.New
        )
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

    // Update sort
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var updateSort: SubmissionSort? by remember { mutableStateOf(null) }
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
            Text(text = "r/${submissionsListViewModel.subreddit}")
        },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.clickable { scrollToTop = true })
    }, bottomBar = {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .height(bottomBarHeight)
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = -bottomBarOffsetHeightPx.roundToInt()) }) {
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
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "")
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "")
                    }
                    FloatingActionButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.ExpandCircleDown,
                            contentDescription = "",
                            modifier = Modifier.scale(scaleX = 1f, scaleY = -1f)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "")
                    }
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "")
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
        if (showBottomSheet) {
            BottomSheet(onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                onNavigateToSettings = onNavigateToSettings,
                onSortSelected = { sort ->
                    updateSort = sort
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onNavigateToSettings: () -> Unit,
    onSortSelected: (SubmissionSort) -> Unit,
) {
    val sortSheetState = rememberModalBottomSheetState()
    var showSortSheet by remember { mutableStateOf(false) }

    if (showSortSheet) {
        SortSelectionBottomSheet(onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState,
            onSortSelected = { sort ->
                onSortSelected(sort)
                onDismissRequest()
            })
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            item {
                BottomSheetGridItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = onNavigateToSettings,
                )
            }
            item {
                BottomSheetGridItem(
                    icon = Icons.AutoMirrored.Filled.Sort,
                    label = "Sort",
                    onClick = { showSortSheet = true },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onSortSelected: (SubmissionSort) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.Default.StarOutline, contentDescription = ""
            )
        }, text = "Best", onClick = {
            onSortSelected(SubmissionSort.Best)
            onDismissRequest()
        })
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.Default.Whatshot, contentDescription = ""
            )
        }, text = "Hot", onClick = {
            onSortSelected(SubmissionSort.Hot)
            onDismissRequest()
        })
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.Default.NewReleases, contentDescription = ""
            )
        }, text = "New", onClick = {
            onSortSelected(SubmissionSort.New)
            onDismissRequest()
        })
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = ""
            )
        }, text = "Rising", onClick = {
            onSortSelected(SubmissionSort.Rising)
            onDismissRequest()
        })
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.Default.Leaderboard, contentDescription = ""
            )
        }, text = "Top", trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown, contentDescription = ""
            )
        })
        BottomSheetItem(leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingDown, contentDescription = ""
            )
        }, text = "Controversial", trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown, contentDescription = ""
            )
        })
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    SubmissionsScaffold(onNavigateToSettings = {})
}