package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

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

class Tomorrow: Theme {
  @ColorInt
  override val foregroundColor: Int = foreground
  @ColorInt
  override val backgroundColor: Int = background
  @ColorInt
  override val reservedColor: Int = purple
  @ColorInt
  override val keywordColor: Int = purple
  @ColorInt
  override val builtinColor: Int = blue
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
  override val specialColor: Int = foregroundColor
  @ColorInt
  override val identifierColor: Int = red
  @ColorInt
  override val preprocessorColor: Int = purple
  @ColorInt
  override val commentColor: Int = comment
  @ColorInt
  override val underlineColor: Int = foregroundColor
  @ColorInt
  override val todoColor: Int = comment

  private companion object {
    val background = Color.parseColor("#ffffff")
    val currentLine = Color.parseColor("#efefef")
    val selection = Color.parseColor("#d6d6d6")
    val foreground = Color.parseColor("#4d4d4c")
    val comment = Color.parseColor("#8e908c")
    val red = Color.parseColor("#c82829")
    val orange = Color.parseColor("#f5871f")
    val yellow = Color.parseColor("#eab700")
    val green = Color.parseColor("#718c00")
    val aqua = Color.parseColor("#3e999f")
    val blue = Color.parseColor("#4271ae")
    val purple = Color.parseColor("#8959a8")
  }
}
