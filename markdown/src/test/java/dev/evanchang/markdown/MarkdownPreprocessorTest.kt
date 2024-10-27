package dev.evanchang.markdown

import org.junit.Test

import org.junit.jupiter.api.Assertions.*

class MarkdownPreprocessorTest {
    private val markdownPreprocessor = MarkdownPreprocessor()

    @Test
    fun blockQuotes() {
        // Multilevel quotes with and without space after
        assertEquals("> Test", markdownPreprocessor.process(">Test"))
        assertEquals(">> Test", markdownPreprocessor.process(">>Test"))
        assertEquals(">>> Test", markdownPreprocessor.process(">>> Test"))

        // &gt;
        assertEquals("> Test", markdownPreprocessor.process("&gt;Test"))

        // ">" not at beginning of line
        assertEquals("> Test>Test > Test", markdownPreprocessor.process(">Test>Test > Test"))
    }

    @Test
    fun headers() {
        // Multilevel headings with and without space after
        assertEquals("# Test", markdownPreprocessor.process("#Test"))
        assertEquals("## Test", markdownPreprocessor.process("##Test"))
        assertEquals("### Test", markdownPreprocessor.process("### Test"))
        assertEquals("###### Test", markdownPreprocessor.process("######Test"))
        assertEquals("###### Test", markdownPreprocessor.process("###### Test"))

        // No h7 heading
        assertEquals("###### #Test", markdownPreprocessor.process("#######Test"))
        assertEquals("###### # Test", markdownPreprocessor.process("####### Test"))

        // "#" not at beginning of line
        assertEquals("# Test#Test # Test", markdownPreprocessor.process("#Test#Test # Test"))
    }
}