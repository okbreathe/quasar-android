package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

class TomorrowNight: Theme {
//	call <SID>X("Comment", s:comment, "", "")
//	call <SID>X("Todo", s:comment, s:background, "")
//	call <SID>X("Title", s:comment, "", "")
//	call <SID>X("Identifier", s:red, "", "none")
//	call <SID>X("Statement", s:foreground, "", "")
//	call <SID>X("Conditional", s:foreground, "", "")
//	call <SID>X("Repeat", s:foreground, "", "")
//	call <SID>X("Structure", s:purple, "", "")
//	call <SID>X("Function", s:blue, "", "")
//	call <SID>X("Constant", s:orange, "", "")
//	call <SID>X("Keyword", s:orange, "", "")
//	call <SID>X("String", s:green, "", "")
//	call <SID>X("Special", s:foreground, "", "")
//	call <SID>X("PreProc", s:purple, "", "")
//	call <SID>X("Operator", s:aqua, "", "none")
//	call <SID>X("Type", s:blue, "", "none")
//	call <SID>X("Define", s:purple, "", "none")
//	call <SID>X("Include", s:blue, "", "")
//	"call <SID>X("Ignore", "666666", "", "")
  @ColorInt
  override val foregroundColor: Int = foreground
  @ColorInt
  override val backgroundColor: Int = background

  @ColorInt
  override val reservedColor: Int = red
  @ColorInt
  override val keywordColor: Int = blue
  @ColorInt
  override val builtinColor: Int = purple
  @ColorInt
  override val typeColor: Int = blue

  @ColorInt
  override val constantColor: Int = orange
  @ColorInt
  override val stringColor: Int = green
  @ColorInt
  override val numberColor: Int = constantColor
  @ColorInt
  override val floatColor: Int = constantColor
  @ColorInt
  override val booleanColor: Int = constantColor

  @ColorInt
  override val specialColor: Int = foreground
  @ColorInt
  override val identifierColor: Int = blue
  @ColorInt
  override val preprocessorColor: Int = orange
  @ColorInt
  override val commentColor: Int = comment
  @ColorInt
  override val underlineColor: Int = foreground
  @ColorInt
  override val todoColor: Int = foreground

  private companion object {
    val background = Color.parseColor("#1d1f21")
    val currentLine = Color.parseColor("#282a2e")
    val selection = Color.parseColor("#373b41")
    val foreground = Color.parseColor("#c5c8c6")
    val comment = Color.parseColor("#969896")
    val red = Color.parseColor("#cc6666")
    val orange = Color.parseColor("#de935f")
    val yellow = Color.parseColor("#f0c674")
    val green = Color.parseColor("#b5bd68")
    val aqua = Color.parseColor("#8abeb7")
    val blue = Color.parseColor("#81a2be")
    val purple = Color.parseColor("#b294bb")
  }
}
