package dev.evanchang.markdown

import android.widget.TextView
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    highlightColor: Color,
    maxLines: Int? = null,
    onClick: (() -> Unit)? = null,
    onLinkClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val markwon: Markwon = remember {
        MarkwonRenderer.create(
            context = context,
            density = density,
            highlightColor = highlightColor,
            onLinkClick = onLinkClick,
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->
            TextView(factoryContext).apply {
                isSoundEffectsEnabled = false
                setTextColor(style.color.toArgb())
            }
        },
        update = { view ->
            if (maxLines != null) {
                view.setMaxLines(maxLines)
            }
            if (onClick != null) {
                view.setOnClickListener { onClick() }
            }
            markwon.setMarkdown(view, markdownText)
        },
        onReset = { view ->
            view.maxLines = Int.MAX_VALUE
            view.setOnClickListener(null)
            view.text = ""
        },
    )
}

@Preview
@Composable
private fun Test() {
    Surface {
        MarkdownText(
            markdownText = "Hello *World*!",
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            highlightColor = MaterialTheme.colorScheme.primary,
        )
    }
}
