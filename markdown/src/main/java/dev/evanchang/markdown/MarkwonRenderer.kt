package dev.evanchang.markdown

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme

internal object MarkwonRenderer {
    fun create(
        context: Context,
        density: Density,
        highlightColor: Color,
        onLinkClick: (String) -> Unit = {},
    ): Markwon {
        return Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun processMarkdown(input: String): String {
                return MarkdownPreprocessorInstance.instance.process(input)
            }

            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.linkResolver { _, link ->
                    onLinkClick(link)
                }
            }

            override fun configureTheme(builder: MarkwonTheme.Builder) {
                with(density) {
                    builder.headingBreakHeight(0).blockQuoteColor(highlightColor.toArgb())
                        .thematicBreakHeight(1.5.dp.roundToPx()).linkColor(highlightColor.toArgb())
                       .build()
                }
            }
        }).build()
    }
}