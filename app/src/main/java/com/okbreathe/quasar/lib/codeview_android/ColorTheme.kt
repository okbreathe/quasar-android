package com.okbreathe.quasar.lib.codeview_android

import com.okbreathe.quasar.components.highlighter.themes.*
import io.github.kbiakov.codeview.highlight.ColorThemeData
import io.github.kbiakov.codeview.highlight.SyntaxColors

private val numColor = 0x99A8B7
private val bgNum = 0xF2F2F5
private val noteColor = 0x4C5D6E

val SOLARIZED_LIGHT by lazy { generateTheme(SolarizedLight()) }

val SOLARIZED_DARK by lazy { generateTheme(SolarizedDark()) }

val GITHUB by lazy { generateTheme(Github()) }

val SYNTHWAVE by lazy { generateTheme(Synthwave()) }

val TOMORROW by lazy { generateTheme(Tomorrow()) }

val TOMORROW_NIGHT by lazy { generateTheme(TomorrowNight()) }

val MONOKAI by lazy { generateTheme(Monokai()) }

private fun generateTheme(theme: Theme) =
  ColorThemeData(
    syntaxColors = SyntaxColors(
      type = 0xFFFFFF and theme.typeColor,
      keyword = 0xFFFFFF and theme.keywordColor,
      literal = 0xFFFFFF and theme.specialColor,
      comment = 0xFFFFFF and theme.commentColor,
      string = 0xFFFFFF and theme.stringColor,
      punctuation = 0xFFFFFF and theme.specialColor,
      plain = 0xFFFFFF and theme.foregroundColor,
      tag = 0xFFFFFF and theme.specialColor,
      declaration = 0xFFFFFF and theme.builtinColor,
      attrName = 0xFFFFFF and theme.identifierColor,
      attrValue = 0xFFFFFF and theme.identifierColor
    ),
    numColor = numColor,
    bgContent = 0xFFFFFF and theme.backgroundColor,
    bgNum = bgNum,
    noteColor = noteColor
  )
