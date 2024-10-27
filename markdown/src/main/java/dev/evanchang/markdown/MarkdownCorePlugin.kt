package dev.evanchang.markdown

import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.SimpleBlockNodeVisitor
import org.commonmark.node.*

internal class MarkdownCorePlugin : CorePlugin() {
    override fun configureVisitor(builder: MarkwonVisitor.Builder) {
        text(builder)
        strongEmphasis(builder)
        emphasis(builder)
        blockQuote(builder)
        code(builder)
        fencedCodeBlock(builder)
        indentedCodeBlock(builder)
        bulletList(builder)
        orderedList(builder)
        listItem(builder)
        thematicBreak(builder)
        heading(builder)
        softLineBreak(builder)
        hardLineBreak(builder)
        paragraph(builder)
        link(builder)
    }

    private fun text(builder: MarkwonVisitor.Builder) {
        builder.on(
            Text::class.java
        ) { visitor, text ->
            val literal = text.literal
            visitor.builder().append(literal)
        }
    }

    private fun strongEmphasis(builder: MarkwonVisitor.Builder) {
        builder.on(
            StrongEmphasis::class.java
        ) { visitor, strongEmphasis ->
            val length = visitor.length()
            visitor.visitChildren(strongEmphasis)
            visitor.setSpansForNodeOptional(strongEmphasis, length)
        }
    }

    private fun emphasis(builder: MarkwonVisitor.Builder) {
        builder.on(
            Emphasis::class.java
        ) { visitor, emphasis ->
            val length = visitor.length()
            visitor.visitChildren(emphasis)
            visitor.setSpansForNodeOptional(emphasis, length)
        }
    }

    private fun blockQuote(builder: MarkwonVisitor.Builder) {
        builder.on(
            BlockQuote::class.java
        ) { visitor, blockQuote ->
            visitor.blockStart(blockQuote)
            val length = visitor.length()

            visitor.visitChildren(blockQuote)
            visitor.setSpansForNodeOptional(blockQuote, length)
            visitor.blockEnd(blockQuote)
        }
    }

    private fun code(builder: MarkwonVisitor.Builder) {
        builder.on(
            Code::class.java
        ) { visitor, code ->
            val length = visitor.length()
            visitor.builder().append('\u00a0').append(code.literal).append('\u00a0')
            visitor.setSpansForNodeOptional(code, length)
        }
    }

    private fun fencedCodeBlock(builder: MarkwonVisitor.Builder) {
        builder.on(
            FencedCodeBlock::class.java
        ) { visitor, fencedCodeBlock ->
            visitCodeBlock(
                visitor, fencedCodeBlock.info, fencedCodeBlock.literal, fencedCodeBlock
            )
        }
    }

    private fun indentedCodeBlock(builder: MarkwonVisitor.Builder) {
        builder.on(
            IndentedCodeBlock::class.java
        ) { visitor, indentedCodeBlock ->
            visitCodeBlock(
                visitor, null, indentedCodeBlock.literal, indentedCodeBlock
            )
        }
    }

    private fun visitCodeBlock(
        visitor: MarkwonVisitor, info: String?, code: String, node: Node
    ) {
        visitor.blockStart(node)

        val length = visitor.length()

        visitor.builder().append('\u00a0').append('\n')
            .append(visitor.configuration().syntaxHighlight().highlight(info, code))

        visitor.ensureNewLine()

        visitor.builder().append('\u00a0')

        CoreProps.CODE_BLOCK_INFO[visitor.renderProps()] = info

        visitor.setSpansForNodeOptional(node, length)

        visitor.blockEnd(node)
    }

    private fun bulletList(builder: MarkwonVisitor.Builder) {
        builder.on(BulletList::class.java, SimpleBlockNodeVisitor())
    }

    private fun orderedList(builder: MarkwonVisitor.Builder) {
        builder.on(OrderedList::class.java, SimpleBlockNodeVisitor())
    }

    private fun listItem(builder: MarkwonVisitor.Builder) {
        builder.on(
            ListItem::class.java
        ) { visitor, listItem ->
            val length = visitor.length()
            // it's important to visit children before applying render props (
            // we can have nested children, who are list items also, thus they will
            // override out props (if we set them before visiting children)
            visitor.visitChildren(listItem)

            val parent: Node = listItem.parent
            if (parent is OrderedList) {
                val start = parent.startNumber

                CoreProps.LIST_ITEM_TYPE[visitor.renderProps()] = CoreProps.ListItemType.ORDERED
                CoreProps.ORDERED_LIST_ITEM_NUMBER[visitor.renderProps()] = start

                // after we have visited the children increment start number
                val orderedList = parent
                orderedList.startNumber += 1
            } else {
                CoreProps.LIST_ITEM_TYPE[visitor.renderProps()] = CoreProps.ListItemType.BULLET
                CoreProps.BULLET_LIST_ITEM_LEVEL[visitor.renderProps()] = listLevel(listItem)
            }

            visitor.setSpansForNodeOptional(listItem, length)
            if (visitor.hasNext(listItem)) {
                visitor.ensureNewLine()
            }
        }
    }

    private fun listLevel(node: Node): Int {
        var level = 0
        var parent = node.parent
        while (parent != null) {
            if (parent is ListItem) {
                level += 1
            }
            parent = parent.parent
        }
        return level
    }

    private fun thematicBreak(builder: MarkwonVisitor.Builder) {
        builder.on(
            ThematicBreak::class.java
        ) { visitor, thematicBreak ->
            visitor.blockStart(thematicBreak)
            val length = visitor.length()

            // without space it won't render
            visitor.builder().append('\u00a0')

            visitor.setSpansForNodeOptional(thematicBreak, length)
            visitor.blockEnd(thematicBreak)
        }
    }

    private fun heading(builder: MarkwonVisitor.Builder) {
        builder.on(
            Heading::class.java
        ) { visitor, heading ->
            visitor.blockStart(heading)
            val length = visitor.length()
            visitor.visitChildren(heading)

            CoreProps.HEADING_LEVEL[visitor.renderProps()] = heading.level

            visitor.setSpansForNodeOptional(heading, length)
            visitor.blockEnd(heading)
        }
    }

    private fun softLineBreak(builder: MarkwonVisitor.Builder) {
        builder.on(
            SoftLineBreak::class.java
        ) { visitor, _ -> visitor.builder().append(' ') }
    }

    private fun hardLineBreak(builder: MarkwonVisitor.Builder) {
        builder.on(
            HardLineBreak::class.java
        ) { visitor, _ -> visitor.ensureNewLine() }
    }

    private fun paragraph(builder: MarkwonVisitor.Builder) {
        builder.on(
            Paragraph::class.java
        ) { visitor, paragraph ->
            val inTightList = isInTightList(paragraph)
            if (!inTightList) {
                visitor.blockStart(paragraph)
            }

            val length = visitor.length()
            visitor.visitChildren(paragraph)

            CoreProps.PARAGRAPH_IS_IN_TIGHT_LIST[visitor.renderProps()] = inTightList

            visitor.setSpansForNodeOptional(paragraph, length)
            if (!inTightList) {
                visitor.blockEnd(paragraph)
            }
        }
    }

    private fun isInTightList(paragraph: Paragraph): Boolean {
        val parent: Node? = paragraph.parent
        if (parent != null) {
            val gramps = parent.parent
            if (gramps is ListBlock) {
                return gramps.isTight
            }
        }
        return false
    }

    private fun link(builder: MarkwonVisitor.Builder) {
        builder.on(
            Link::class.java
        ) { visitor, link ->
            val length = visitor.length()
            visitor.visitChildren(link)

            val destination = link.destination

            CoreProps.LINK_DESTINATION[visitor.renderProps()] = destination
            visitor.setSpansForNodeOptional(link, length)
        }
    }
}