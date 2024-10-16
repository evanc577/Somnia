package dev.evanchang.somnia.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.evanchang.somnia.ui.submissions.Submissions

val TOP_BAR_HEIGHT = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    val listState = rememberLazyStaggeredGridState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val topBarHeight = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(
            LocalDensity.current
        ).toDp() + TOP_BAR_HEIGHT
    }

    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        FlexibleTopBar(scrollBehavior = scrollBehavior,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            content = {
                Box(modifier = Modifier.height(topBarHeight)) {}
            })
    }) { padding ->
        Submissions(listState = listState, padding = padding, topBarHeight = topBarHeight)
    }
}