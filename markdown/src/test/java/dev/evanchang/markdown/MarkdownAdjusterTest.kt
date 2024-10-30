package dev.evanchang.markdown

import org.junit.Test

import org.junit.jupiter.api.Assertions.*

class MarkdownAdjusterTest {
    private val markdownAdjuster = MarkdownAdjuster()

    @Test
    fun blockQuotes() {
        // Multilevel quotes with and without space after
        assertEquals("> Test", markdownAdjuster.preprocess(">Test"))
        assertEquals(">> Test", markdownAdjuster.preprocess(">>Test"))
        assertEquals(">>> Test", markdownAdjuster.preprocess(">>> Test"))

        // &gt;
        assertEquals("> Test", markdownAdjuster.preprocess("&gt;Test"))

        // ">" not at beginning of line
        assertEquals("> Test>Test > Test", markdownAdjuster.preprocess(">Test>Test > Test"))
    }

    @Test
    fun headers() {
        // Multilevel headings with and without space after
        assertEquals("# Test", markdownAdjuster.preprocess("#Test"))
        assertEquals("## Test", markdownAdjuster.preprocess("##Test"))
        assertEquals("### Test", markdownAdjuster.preprocess("### Test"))
        assertEquals("###### Test", markdownAdjuster.preprocess("######Test"))
        assertEquals("###### Test", markdownAdjuster.preprocess("###### Test"))

        // No h7 heading
        assertEquals("###### #Test", markdownAdjuster.preprocess("#######Test"))
        assertEquals("###### # Test", markdownAdjuster.preprocess("####### Test"))

        // "#" not at beginning of line
        assertEquals("# Test#Test # Test", markdownAdjuster.preprocess("#Test#Test # Test"))
    }
}