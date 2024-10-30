package dev.evanchang.markdown

import android.content.Context
import android.graphics.Typeface
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.PrecomputedTextSetterCompat
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.utils.NoCopySpannableFactory
import java.util.concurrent.Executors

internal object MarkwonRenderer {
    fun create(
        context: Context,
        density: Density,
        style: TextStyle,
        typeface: Typeface,
        highlightColor: Color,
        onLinkClick: (String) -> Unit = {},
    ): Markwon {
        return Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun processMarkdown(input: String): String {
                return adjuster.preprocess(input)
            }

            override fun beforeSetText(textView: TextView, markdown: Spanned) {
                setTextViewOptions(textView, style, typeface)
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
        }).textSetter(PrecomputedTextSetterCompat.create(Executors.newCachedThreadPool())).build()
    }

    val adjuster = MarkdownAdjuster()
}

internal fun setTextViewOptions(textView: TextView, style: TextStyle, typeface: Typeface) {
    textView.setTextColor(style.color.toArgb())
    textView.textSize = style.fontSize.value
    textView.typeface = typeface
    textView.movementMethod = LinkMovementMethod.getInstance()
    textView.setSpannableFactory(NoCopySpannableFactory.getInstance())
}