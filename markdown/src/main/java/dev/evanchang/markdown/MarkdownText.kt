package dev.evanchang.markdown

import android.widget.TextView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit = {},
) {
    val context = LocalContext.current

    val markwon: Markwon = remember {
        MarkwonRenderer.create(
            context, onLinkClick = onLinkClick
        )
    }

    AndroidView(modifier = modifier, factory = { factoryContext ->
        TextView(factoryContext)
    }, update = { textView ->
        markwon.setMarkdown(textView, markdownText)
    })
}

@Preview
@Composable
private fun Test() {
    Surface {
        MarkdownText(markdownText = "Hello *World*!")
    }
}
