package com.okbreathe.quasar.lib.commonmark_ext_media.internal

import com.okbreathe.quasar.lib.commonmark_ext_media.MediaBlock
import org.commonmark.node.Block
import org.commonmark.parser.block.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MediaBlockParser(val title: String?, val url: String?) : AbstractBlockParser() {
  private val block = MediaBlock(title, url)

  override fun getBlock(): Block = block

  override fun tryContinue(parserState: ParserState) = BlockContinue.none()

  class Factory : AbstractBlockParserFactory() {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
      if (state.indent >= 4) return BlockStart.none()

      val line = state.line
      val nextNonSpace = state.nextNonSpaceIndex
      val matcher: Matcher = MEDIA.matcher(line.subSequence(nextNonSpace, line.length))

      return if (matcher.find()) {
        val title = matcher.group(1)
        val url = matcher.group(2)
        BlockStart.of(MediaBlockParser(title, "http://10.0.2.2:4000$url")).atIndex(line.length)
      } else {
        BlockStart.none()
      }
    }
  }

  companion object {
    private val MEDIA = Pattern.compile("^!\\[([^\\]]+)\\]\\(([^\\)]+)\\)(?:[ \t]+|$)")
  }
}
