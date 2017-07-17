package com.okbreathe.quasar.components.highlighter.grammars

import java.util.regex.Pattern

class JavaScriptGrammar: Grammar {
  override fun numberPattern(): Pattern = PATTERN_NUMBERS
  override fun preprocessorPattern(): Pattern = PATTERN_PREPROCESSOR
  override fun keywordPattern(): Pattern = PATTERN_KEYWORD
  override fun builtinPattern(): Pattern = PATTERN_BUILTIN
  override fun commentPattern(): Pattern = PATTERN_COMMENT
  override fun stringPattern(): Pattern = PATTERN_STRING
  override fun specialPattern(): Pattern = PATTERN_DELIMITER
  override fun identifierPattern(): Pattern = PATTERN_IDENTIFIER

  companion object {
    //Default Highlighting definitions
    private val PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b")
    private val PATTERN_PREPROCESSOR = Pattern.compile(
      "^[\t ]*(#define|#undef|#if|#ifdef|#ifndef|#else|#elif|#endif|" + "#error|#pragma|#extension|#version|#line)\\b",
      Pattern.MULTILINE)
    private val PATTERN_STRING = Pattern.compile("\"((\\\\[^\\n]|[^\"\\n])*)\"")
    private val PATTERN_KEYWORD = Pattern.compile(
      "\\b(let|var|try|catch|break|continue|" + "do|for|while|if|else|switch|in|out|inout|float|int|void|bool|true|false|new|function)\\b")
    private val PATTERN_BUILTIN = Pattern.compile(
      "\\b(radians|degrees|sin|cos|tan|asin|acos|atan|pow|JSON|document|window|location|console)\\b")
    private val PATTERN_COMMENT = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*")
    private val PATTERN_DELIMITER = Pattern.compile("(\\{|\\}\\)|\\()")
    private val PATTERN_IDENTIFIER = Pattern.compile("a^")
  }
}
