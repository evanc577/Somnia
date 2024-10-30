package dev.evanchang.markdown

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.TextView
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.recycler.MarkwonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.node.Node

@Composable
fun MarkdownText(
    markdownText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    highlightColor: Color,
    previewLines: Int? = null,
    onClick: (() -> Unit)? = null,
    onLinkClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current

    var markwon: Markwon? by remember { mutableStateOf(null) }
    var parsed: Node? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            val typeface = resolver.resolve(
                fontFamily = style.fontFamily,
                fontWeight = style.fontWeight ?: FontWeight.Normal,
                fontStyle = style.fontStyle ?: FontStyle.Normal,
                fontSynthesis = style.fontSynthesis ?: FontSynthesis.All
            ).value as Typeface

            markwon = MarkwonRenderer.create(
                context = context,
                density = density,
                style = style,
                typeface = typeface,
                highlightColor = highlightColor,
                onLinkClick = onLinkClick,
            )

            parsed = markwon!!.parse(
                markdownText
            )
        }
    }

    if (markwon != null && parsed != null) {
        if (previewLines != null) {
            val text = remember {
                val rendered = markwon!!.render(parsed!!)
                val text = rendered.codePoints().limit((previewLines * 100).toLong()).collect(
                    ::StringBuilder, StringBuilder::appendCodePoint, StringBuilder::append
                ).toString()
                MarkwonRenderer.adjuster.processPreview(text)
            }

            AndroidView(
                modifier = modifier,
                factory = { factoryContext ->
                    TextView(factoryContext).apply {
                        setTextViewOptions(this, style, typeface)
                        isSoundEffectsEnabled = false
                        maxLines = previewLines
                        if (onClick != null) {
                            setOnClickListener { onClick() }
                        }
                    }
                },
                update = { textView ->
                    textView.text = text
                },
            )
        } else {
            val adapter = remember {
                MarkwonAdapter.create(R.layout.markdown_textview_item, R.id.textview)
            }
            adapter.setParsedMarkdown(markwon!!, parsed!!)
            adapter.notifyDataSetChanged()

            AndroidView(
                modifier = modifier,
                factory = { factoryContext ->
                    @SuppressLint("InflateParams") val layout = LayoutInflater.from(factoryContext)
                        .inflate(R.layout.markdown_recyclerview, null, false) as ConstraintLayout
                    layout.apply {
                        if (onClick != null) {
                            setOnClickListener { onClick() }
                        }
                    }
                    layout.findViewById<RecyclerView>(R.id.recyclerview).apply {
                        this.layoutManager = LinearLayoutManager(factoryContext)
                        this.itemAnimator = null
                    }
                    layout
                },
                update = {
                    it.findViewById<RecyclerView>(R.id.recyclerview).apply {
                        this.adapter = adapter
                    }
                },
            )
        }
    }
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