package dev.evanchang.markdown

import com.google.re2j.Pattern

internal class MarkdownPreprocessor {
    // Replace escaped "&lt;" with "<" and "&gt;" with ">"
    private val lessThanPattern = Pattern.compile("&lt;")
    private val greaterThanPattern = Pattern.compile("&gt;")
    // Accept block quotes with no space after ">"
    private val blockQuotePattern = Pattern.compile("^(>+)([^ >])", Pattern.MULTILINE)
    // Accept headings with no space after "#"
    private val headingPattern = Pattern.compile("^(#{1,5})([^ #])|^(#{6})([^ ])", Pattern.MULTILINE)

    fun process(input: String): String {
        var markdown = input

        markdown = lessThanPattern.matcher(markdown).replaceAll("<")
        markdown = greaterThanPattern.matcher(markdown).replaceAll(">")
        markdown = blockQuotePattern.matcher(markdown).replaceAll("$1 $2")
        markdown = headingPattern.matcher(markdown).replaceAll("$1$3 $2$4")

        return markdown
    }
}

internal object MarkdownPreprocessorInstance {
    val instance = MarkdownPreprocessor()
}
