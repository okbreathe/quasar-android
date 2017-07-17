package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

class Monokai: Theme {
  @ColorInt
  override val foregroundColor: Int = light_ghost_white
  @ColorInt
  override val backgroundColor: Int = dark_gray

  @ColorInt
  override val reservedColor: Int = green
  @ColorInt
  override val keywordColor: Int = reservedColor
  @ColorInt
  override val builtinColor: Int = pink
  @ColorInt
  override val typeColor: Int = blue

  @ColorInt
  override val constantColor: Int = purple
  @ColorInt
  override val stringColor: Int = yellow
  @ColorInt
  override val numberColor: Int = constantColor
  @ColorInt
  override val floatColor: Int = constantColor
  @ColorInt
  override val booleanColor: Int = constantColor

  @ColorInt
  override val specialColor: Int = light_gray
  @ColorInt
  override val identifierColor: Int = orange
  @ColorInt
  override val preprocessorColor: Int = light_gray
  @ColorInt
  override val commentColor: Int = brown
  @ColorInt
  override val underlineColor: Int = blue
  @ColorInt
  override val todoColor: Int = yellow

  private companion object {
    // https://github.com/kevinsawicki/monokai
//    val ghost_white = Color.parseColor("#F8F8F0")
    val light_ghost_white = Color.parseColor("#F8F8F2")
    val light_gray = Color.parseColor("#CCCCCC")
    val gray = Color.parseColor("#888888")
//    val brown_gray = Color.parseColor("#49483E")
    val dark_gray = Color.parseColor("#282828")

    val yellow = Color.parseColor("#E6DB74")
    val blue = Color.parseColor("#66D9EF")
    val pink = Color.parseColor("#F92672")
    val purple = Color.parseColor("#AE81FF")
    val brown = Color.parseColor("#75715E")
    val orange = Color.parseColor("#FD971F")
//    val light_orange = Color.parseColor("#FFD569")
    val green = Color.parseColor("#A6E22E")
//    val sea_green = Color.parseColor("#529B2F")
  }
}
