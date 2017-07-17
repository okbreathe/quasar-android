package com.okbreathe.quasar.lib.commonmark_ext_media.internal

import org.commonmark.node.Block
import org.commonmark.node.Heading
import org.commonmark.parser.InlineParser
import org.commonmark.parser.block.*

import java.util.regex.Matcher
import java.util.regex.Pattern

class HeadingParser(private val content: String) : AbstractBlockParser() {

  private val block = Heading()

  override fun getBlock(): Block = block

  override fun tryContinue(parserState: ParserState) = BlockContinue.none()

  override fun parseInlines(inlineParser: InlineParser?) {
    inlineParser!!.parse(content, block)
  }

  class Factory : AbstractBlockParserFactory() {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart {
      if (state.indent >= 4) return BlockStart.none()

      val line = state.line
      val nextNonSpace = state.nextNonSpaceIndex
      val matcher: Matcher = MEDIA.matcher(line.subSequence(nextNonSpace, line.length))

      if (matcher.find()) {
        val newOffset = nextNonSpace + matcher.group(0).length
        val content = line.subSequence(newOffset, line.length).toString()
        return BlockStart.of(HeadingParser(content)).atIndex(line.length)
      } else {
        return BlockStart.none()
      }
    }
  }

  companion object {
    private val MEDIA = Pattern.compile("^#{1,6}(?:[ \t]+|$)")
  }
}
