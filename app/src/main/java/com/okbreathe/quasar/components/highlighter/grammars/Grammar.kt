package com.okbreathe.quasar.components.highlighter.grammars

import java.util.regex.Pattern

/**
 * Based on http://vimdoc.sourceforge.net/htmldoc/syntax.html#syntax
 *
 * Constant = [String, Number, Float, Boolean]
 * Identifier
 * Statement (Renamed Reserved) = [Keyword, Builtin, Type]
 * Preprocessor
 * Type
 * Special
 * Underlined
 * Ignored -> Unimplemented
 * Error -> Unimplemented
 * Comment
 * Todo
 */
interface Grammar {
  // Reserved words: keywords and builtins
  fun reservedPattern(): Pattern = NONE
  // Keywords: core language constructs handled by the parser. Reserved and cannot be used as identifiers
  fun keywordPattern(): Pattern = reservedPattern()
  // Builtins: commonly used, preloaded functions, constants, types, and exceptions
  fun builtinPattern(): Pattern = reservedPattern()
  // Types: int, long, chat etc.
  fun typePattern(): Pattern = reservedPattern()

  // Constants: strings, numbers, floats and booleans
  fun constantPattern(): Pattern = NONE
  // String: e.g. "this is a string"
  fun stringPattern(): Pattern = constantPattern()
  // Number: e.g. 234, 0xff
  fun numberPattern(): Pattern = constantPattern()
  // Float: e.g 2.3e10
  fun floatPattern(): Pattern = numberPattern()
  // Boolean: e.g. TRUE, false
  fun booleanPattern(): Pattern = constantPattern()

  // Special: special symbols e.g. delimiters
  fun specialPattern(): Pattern = NONE

  // Identifier: e.g variable names, functions, class methods
  fun identifierPattern(): Pattern = NONE

  // Newlines
  fun newlinePattern(): Pattern = NEWLINE

  // Preprocessor statements: e.g. #include,  #define
  fun preprocessorPattern(): Pattern = NONE

  // Comments
  fun commentPattern(): Pattern = NONE

  // Underlined - text that stands out, e.g. links
  fun underlinePattern(): Pattern = NONE

  // Todo - anything that needs extra attention e.g. TODO, FIXME, XXX
  fun todoPattern(): Pattern = NONE

  companion object {
    private val NEWLINE = Pattern.compile(".*\\n")
    private val NONE = Pattern.compile("a^")
  }
}


