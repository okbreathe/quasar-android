package com.okbreathe.quasar.lib.commonmark_ext_media.internal

import org.commonmark.internal.util.Parsing
import org.commonmark.node.Block
import org.commonmark.node.BlockQuote
import org.commonmark.parser.block.*

class BlockQuoteParser : AbstractBlockParser() {

  private val block = BlockQuote()

  override fun isContainer(): Boolean {
    return true
  }

  override fun canContain(block: Block?): Boolean {
    return true
  }

  override fun getBlock(): BlockQuote {
    return block
  }

  override fun tryContinue(state: ParserState): BlockContinue {
    val nextNonSpace = state.nextNonSpaceIndex
    if (isMarker(state, nextNonSpace)) {
      var newColumn = state.column + state.indent + 1
      // optional following space or tab
      if (Parsing.isSpaceOrTab(state.line, nextNonSpace + 1)) {
        newColumn++
      }
      return BlockContinue.atColumn(newColumn)
    } else {
      return BlockContinue.none()
    }
  }

  class Factory : AbstractBlockParserFactory() {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart {
      val nextNonSpace = state.nextNonSpaceIndex
      if (isMarker(state, nextNonSpace)) {
        var newColumn = state.column + state.indent + 1
        // optional following space or tab
        if (Parsing.isSpaceOrTab(state.line, nextNonSpace + 1)) {
          newColumn++
        }
        return BlockStart.of(BlockQuoteParser()).atColumn(newColumn)
      } else {
        return BlockStart.none()
      }
    }
  }

  companion object {

    private fun isMarker(state: ParserState, index: Int): Boolean {
      val line = state.line
      return state.indent < Parsing.CODE_BLOCK_INDENT && index < line.length && line[index] == '>'
    }
  }
}
