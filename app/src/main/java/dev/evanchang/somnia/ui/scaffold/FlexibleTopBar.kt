package dev.evanchang.somnia.ui.scaffold

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt

/**
 * This top bar uses the same scroll behaviors as Material3 top bars,
 * but it doesn't have a layout of its own. It is simply a container in
 * which you can put whatever you want.
 */
@ExperimentalMaterial3Api
@Composable
fun FlexibleTopBar(
    modifier: Modifier = Modifier,
    color: Color,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable () -> Unit,
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    var heightOffsetLimit by remember {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(heightOffsetLimit) {
        if (scrollBehavior?.state?.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit
        }
    }

    // Compose a Surface with a TopAppBarLayout content.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    Surface(color = color) {
        Layout(content = content, modifier = modifier, measurePolicy = { measurables, constraints ->
            val placeable = measurables.first().measure(constraints.copy(minWidth = 0))
            heightOffsetLimit = placeable.height.toFloat() * -1
            val scrollOffset = scrollBehavior?.state?.heightOffset ?: 0f
            val height = placeable.height.toFloat() + scrollOffset
            val layoutHeight = height.roundToInt().coerceAtLeast(0)
            layout(constraints.maxWidth, layoutHeight) {
                placeable.place(0, scrollOffset.toInt())
            }
        })
    }
}