package dev.evanchang.markdown

import android.content.Context
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration

internal object MarkwonRenderer {
    fun create(
        context: Context,
        onLinkClick: (String) -> Unit = {},
    ): Markwon {
        return Markwon.builder(context).usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                builder.linkResolver { _, link ->
                    onLinkClick(link)
                }
            }
        }).build()
    }
}