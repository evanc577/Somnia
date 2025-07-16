package dev.evanchang.somnia.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState

@Composable
fun SomniaMarkdown(content: String, isPreview: Boolean, modifier: Modifier = Modifier) {
    if (isPreview) {
        Text(
            text = content.buildMarkdownAnnotatedString(
                markdownTypography().text
            ),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    } else {
        val markdownState = rememberMarkdownState(content)
        Markdown(
            markdownState = markdownState,
            imageTransformer = Coil3ImageTransformerImpl,
            typography = markdownTypography(link = TextStyle(color = MaterialTheme.colorScheme.primary)),
            modifier = modifier,
        )
    }
}