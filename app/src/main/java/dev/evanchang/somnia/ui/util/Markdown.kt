package dev.evanchang.somnia.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random

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
        val vmKey = rememberSaveable { Random.nextInt().toString() }
        val vm = viewModel(key = vmKey) { MarkdownViewModel(content) }
        val state by vm.markdownState.collectAsStateWithLifecycle()

        Markdown(
            state = state,
            imageTransformer = Coil3ImageTransformerImpl,
            typography = markdownTypography(link = TextStyle(color = MaterialTheme.colorScheme.primary)),
            modifier = modifier,
        )
    }
}

private class MarkdownViewModel(content: String) : ViewModel() {
    val markdownState = parseMarkdownFlow(content)
        .stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading())
}