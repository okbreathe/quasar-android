package com.okbreathe.quasar.components.highlighter.grammars

import java.util.regex.Pattern

class MarkdownGrammar: Grammar {
  override fun reservedPattern(): Pattern = HEADING

  override fun keywordPattern(): Pattern = reservedPattern()

  override fun builtinPattern(): Pattern = LINK

  override fun typePattern(): Pattern = reservedPattern()

  override fun constantPattern(): Pattern = UL_LIST

  override fun stringPattern(): Pattern = BOLD

  override fun numberPattern(): Pattern = EMPHASIS

  override fun floatPattern(): Pattern = numberPattern()

  override fun booleanPattern(): Pattern = BLOCKQUOTES

  override fun specialPattern(): Pattern = STRIKETHRU

  override fun identifierPattern(): Pattern = HORIZONTAL_RULE

  override fun commentPattern(): Pattern = BLOCKQUOTES

//  override fun underlinePattern(): Pattern = LINK

  companion object {
    private val HEADING = Pattern.compile("(#+)(.*)")
    private val LINK = Pattern.compile("\\[[^\\]]+\\]\\([^\\)]+\\)")
    private val MEDIA = Pattern.compile("!\\[[^\\]]+\\]\\([^\\)]+\\)")
    private val BOLD = Pattern.compile("(\\*\\*|__)(.*?)\\1")
    private val EMPHASIS = Pattern.compile("(\\*|_)(.*?)\\1")
    private val STRIKETHRU = Pattern.compile("\\~\\~(.*?)\\~\\~")
    private val INLINE_CODE = Pattern.compile("`(.*?)`")
    private val UL_LIST = Pattern.compile("\\n\\*(.*)")
    private val OL_LIST = Pattern.compile("\\n[0-9]+\\.(.*)")
    private val QUOTE = Pattern.compile(">\\s+.*")
    private val BLOCKQUOTES = Pattern.compile("\\n\\>(.*)")
    private val HORIZONTAL_RULE = Pattern.compile("\\n-{5,}")
  }
}
