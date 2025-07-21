package dev.evanchang.somnia.ui.redditscreen.subreddit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import dev.evanchang.somnia.data.SortDuration
import dev.evanchang.somnia.data.SubmissionSort
import dev.evanchang.somnia.dataStore
import dev.evanchang.somnia.navigation.Nav
import dev.evanchang.somnia.ui.UiConstants.CARD_PADDING
import dev.evanchang.somnia.ui.UiConstants.DIALOG_HEADER_SPACING
import dev.evanchang.somnia.ui.settings.screen.SettingsNavKey
import dev.evanchang.somnia.ui.util.BottomSheetGridItem
import dev.evanchang.somnia.ui.util.BottomSheetItem
import kotlin.math.roundToInt

private val BOTTOM_BAR_HEIGHT = 80.dp

class BottomBarNestedScrollConnection(
    private val bottomBarOffsetHeightPx: MutableFloatState,
    private val bottomBarHeightPx: Float,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        bottomBarOffsetHeightPx.floatValue =
            (bottomBarOffsetHeightPx.floatValue + available.y).coerceIn(
                -bottomBarHeightPx, 0f
            )
        return Offset.Zero
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubredditScreen(
    subreddit: String,
    onBack: (Int) -> Unit,
    onNavigate: (Nav) -> Unit,
) {
    val context = LocalContext.current

    val vm: SubredditViewModel = viewModel(
        factory = SubredditViewModel.Factory(
            subreddit = subreddit,
            settings = context.dataStore.data,
        )
    )
    val density = LocalDensity.current

    // Scrolling
    val lazyPagingItems = vm.submissions.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    var scrollToTop by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            listState.scrollToItem(0)
            scrollToTop = false
        }
    }

    // Top bar
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null,
    )
    var topPadding by remember { mutableStateOf(0.dp) }
    val statusBarHeightPx = WindowInsets.safeDrawing.getTop(density)

    // Bottom bar
    val navBarHeightPx = WindowInsets.safeDrawing.getBottom(density)
    val bottomBarHeight by remember(navBarHeightPx) { mutableStateOf(with(density) { navBarHeightPx.toDp() + BOTTOM_BAR_HEIGHT }) }
    val bottomBarHeightPx by remember(bottomBarHeight) {
        mutableFloatStateOf(with(density) {
            bottomBarHeight.roundToPx().toFloat()
        })
    }
    val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(bottomBarHeightPx) {
        BottomBarNestedScrollConnection(bottomBarOffsetHeightPx, bottomBarHeightPx)
    }

    // Update sort
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var updateSort: SubmissionSort? by remember { mutableStateOf(null) }
    LaunchedEffect(updateSort) {
        val updateSortVal = updateSort ?: return@LaunchedEffect
        vm.setSort(updateSortVal)
        vm.setIsRefreshing(true)
        lazyPagingItems.refresh()
    }

    // Exit confirmation if this is the top level screen
    val snackbarHostState = remember { SnackbarHostState() }

    // UI
    Scaffold(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.offset {
                    IntOffset(
                        x = 0,
                        y = bottomBarOffsetHeightPx.floatValue.unaryMinus()
                            .coerceAtMost(BOTTOM_BAR_HEIGHT.toPx()).roundToInt(),
                    )
                },
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    if (vm.subreddit.isEmpty()) {
                        Text(text = "Frontpage")
                    } else {
                        Text(text = "r/${vm.subreddit}")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                modifier = Modifier.clickable { scrollToTop = true },
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .height(bottomBarHeight)
                    .fillMaxWidth()
                    .offset {
                        IntOffset(
                            x = 0, y = -bottomBarOffsetHeightPx.floatValue.roundToInt()
                        )
                    },
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
        },
    ) { padding ->
        // Only use top bar padding without status bar
        topPadding = remember(padding) {
            (padding.calculateTopPadding() - with(density) { statusBarHeightPx.toDp() }).coerceAtLeast(
                0.dp
            )
        }
        Column {
            Spacer(modifier = Modifier.height(with(density) { statusBarHeightPx.toDp() }))
            SubredditList(
                vm = vm,
                listState = listState,
                topPadding = topPadding,
                bottomPadding = with(density) { navBarHeightPx.toDp() },
                onBack = onBack,
                onNavigate = onNavigate,
            )
        }
        if (showBottomSheet) {
            BottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                onNavigateToSettings = { onNavigate(Nav.Settings(SettingsNavKey.TopLevel)) },
                onSortSelected = { sort ->
                    updateSort = sort
                },
                onScrollToTop = {
                    scrollToTop = true
                    showBottomSheet = false
                },
                onGoToSubreddit = { subreddit ->
                    onNavigate(Nav.Subreddit(subreddit))
                },
            )
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
    onScrollToTop: () -> Unit,
    onGoToSubreddit: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val sortSheetState = rememberModalBottomSheetState()
    var showSortSheet by remember { mutableStateOf(false) }
    if (showSortSheet) {
        SortSelectionBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sortSheetState,
            onSortSelected = { sort ->
                onSortSelected(sort)
                onDismissRequest()
            })
    }

    var showGoToSubreddit by remember { mutableStateOf(false) }
    var goToSubredditValue by remember { mutableStateOf("") }
    LaunchedEffect(showGoToSubreddit) {
        if (showGoToSubreddit) {
            focusRequester.requestFocus()
        }
    }
    if (showGoToSubreddit) {
        Dialog(onDismissRequest = { showGoToSubreddit = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(CARD_PADDING)) {
                    Text(text = "Go to subreddit", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(DIALOG_HEADER_SPACING))
                    OutlinedTextField(
                        value = goToSubredditValue,
                        onValueChange = { goToSubredditValue = it },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            onDismissRequest()
                            onGoToSubreddit(goToSubredditValue)
                        }),
                        singleLine = true,
                        modifier = Modifier.focusRequester(focusRequester),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismissRequest) {
                            Text(text = "Cancel")
                        }
                        TextButton(onClick = {
                            onDismissRequest()
                            onGoToSubreddit(goToSubredditValue)
                        }) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
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
            item {
                BottomSheetGridItem(
                    icon = Icons.Default.KeyboardDoubleArrowUp,
                    label = "Scroll to top",
                    onClick = onScrollToTop,
                )
            }
            item {
                BottomSheetGridItem(
                    icon = Icons.Default.Forum,
                    label = "Go to subreddit",
                    onClick = { showGoToSubreddit = true },
                )
            }
        }
    }
}

private enum class SortExpansion {
    COLLAPSED,
    TOP,
    CONTROVERSIAL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onSortSelected: (SubmissionSort) -> Unit,
) {
    var sortExpansion by remember { mutableStateOf(SortExpansion.COLLAPSED) }
    LaunchedEffect(sortExpansion) {
        if (sortExpansion != SortExpansion.COLLAPSED) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.StarOutline, contentDescription = ""
                )
            },
            text = "Best",
            onClick = {
                onSortSelected(SubmissionSort.Best)
                onDismissRequest()
            },
        )
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Whatshot, contentDescription = ""
                )
            },
            text = "Hot",
            onClick = {
                onSortSelected(SubmissionSort.Hot)
                onDismissRequest()
            },
        )
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.NewReleases, contentDescription = ""
                )
            },
            text = "New",
            onClick = {
                onSortSelected(SubmissionSort.New)
                onDismissRequest()
            },
        )
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = ""
                )
            },
            text = "Rising",
            onClick = {
                onSortSelected(SubmissionSort.Rising)
                onDismissRequest()
            },
        )
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Leaderboard, contentDescription = ""
                )
            },
            text = "Top",
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = ""
                )
            },
            onClick = {
                if (sortExpansion != SortExpansion.TOP) {
                    sortExpansion = SortExpansion.TOP
                } else {
                    sortExpansion = SortExpansion.COLLAPSED
                }
            }
        )
        if (sortExpansion == SortExpansion.TOP) {
            SortDurationSelection(onDurationSelected = { duration ->
                onSortSelected(SubmissionSort.Top(duration))
                onDismissRequest()
            })
        }
        BottomSheetItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingDown, contentDescription = ""
                )
            },
            text = "Controversial",
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, contentDescription = ""
                )
            },
            onClick = {
                if (sortExpansion != SortExpansion.CONTROVERSIAL) {
                    sortExpansion = SortExpansion.CONTROVERSIAL
                } else {
                    sortExpansion = SortExpansion.COLLAPSED
                }
            }
        )
        if (sortExpansion == SortExpansion.CONTROVERSIAL) {
            SortDurationSelection(onDurationSelected = { duration ->
                onSortSelected(SubmissionSort.Controversial(duration))
                onDismissRequest()
            })
        }
    }
}

@Composable
private fun SortDurationSelection(
    onDurationSelected: (SortDuration) -> Unit,
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim)) {
        BottomSheetItem(
            leadingIcon = {},
            text = "Hour",
            onClick = { onDurationSelected(SortDuration.HOUR) },
        )
        BottomSheetItem(
            leadingIcon = {},
            text = "Day",
            onClick = { onDurationSelected(SortDuration.DAY) },
        )
        BottomSheetItem(
            leadingIcon = {},
            text = "Week",
            onClick = { onDurationSelected(SortDuration.WEEK) },
        )
        BottomSheetItem(
            leadingIcon = {},
            text = "Month",
            onClick = { onDurationSelected(SortDuration.MONTH) },
        )
        BottomSheetItem(
            leadingIcon = {},
            text = "Year",
            onClick = { onDurationSelected(SortDuration.YEAR) },
        )
        BottomSheetItem(
            leadingIcon = {},
            text = "All Time",
            onClick = { onDurationSelected(SortDuration.ALL) },
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    SubredditScreen(
        subreddit = "dreamcatcher",
        onBack = {},
        onNavigate = {},
    )
}