package dev.evanchang.somnia.ui.scaffold

import android.util.Log
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import dev.evanchang.somnia.ui.submissions.Submissions
import kotlin.math.roundToInt

val TOP_BAR_HEIGHT = 64.dp
val BOTTOM_BAR_HEIGHT = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScaffold() {
    val density = LocalDensity.current
    val listState = rememberLazyStaggeredGridState()

    val statusBarHeightPx = WindowInsets.safeDrawing.getTop(density)
    val navBarHeightPx = WindowInsets.safeDrawing.getBottom(density)

    val topBarHeight = with(density) { statusBarHeightPx.toDp() + TOP_BAR_HEIGHT }
    val bottomBarHeight = with(density) { navBarHeightPx.toDp() + BOTTOM_BAR_HEIGHT }

    val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx().toFloat() }
    var tmpBottomBarOffsetHeightPx by remember { mutableStateOf(0f) }
    var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }

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

    Scaffold(modifier = Modifier.nestedScroll(nestedScrollConnection), bottomBar = {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .height(bottomBarHeight)
                .fillMaxWidth()
                .offset({ IntOffset(x = 0, y = -bottomBarOffsetHeightPx.roundToInt()) })
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
                            Icon(Icons.Default.Star, contentDescription = "")
                        }
                    }
                }
            }
        }
    }) { padding ->
        Column {
            Spacer(modifier = Modifier.height(with(density) { statusBarHeightPx.toDp() }))
            Submissions(listState = listState, padding = padding)
        }
    }
}