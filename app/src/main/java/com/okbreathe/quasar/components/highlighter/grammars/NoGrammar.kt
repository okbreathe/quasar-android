package com.okbreathe.quasar.components.highlighter.grammars

import java.util.regex.Pattern

class NoGrammar: Grammar {
  override fun numberPattern(): Pattern = NUMBERS
  companion object {
    private val NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b")
  }
}
