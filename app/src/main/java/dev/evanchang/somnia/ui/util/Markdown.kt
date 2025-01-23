package dev.evanchang.somnia.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.utils.buildMarkdownAnnotatedString

@Composable
fun SomniaMarkdown(content: String, isPreview: Boolean, modifier: Modifier = Modifier) {
    if (isPreview) {
        Text(
            text = content.buildMarkdownAnnotatedString(markdownTypography().text),
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    } else {
        Markdown(
            content = content,
            colors = markdownColor(linkText = MaterialTheme.colorScheme.primary),
            typography = markdownTypography(),
            modifier = modifier,
        )
    }
}