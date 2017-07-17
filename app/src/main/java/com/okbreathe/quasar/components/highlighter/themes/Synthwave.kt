package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

class Synthwave: Theme {
  @ColorInt
  override val foregroundColor: Int = white
  @ColorInt
  override val backgroundColor: Int = black
  @ColorInt
  override val reservedColor: Int = blue
  @ColorInt
  override val keywordColor: Int = lightRed
  @ColorInt
  override val builtinColor: Int = lightRed
  @ColorInt
  override val typeColor: Int = lightYellow
  @ColorInt
  override val constantColor: Int = cyan
  @ColorInt
  override val stringColor: Int = green
  @ColorInt
  override val numberColor: Int = darkYellow
  @ColorInt
  override val floatColor: Int = darkYellow
  @ColorInt
  override val booleanColor: Int = darkYellow
  @ColorInt
  override val specialColor: Int = blue
  @ColorInt
  override val identifierColor: Int = turqoise
  @ColorInt
  override val preprocessorColor: Int = lightYellow
  @ColorInt
  override val commentColor: Int = commentGrey
  @ColorInt
  override val underlineColor: Int = foregroundColor
  @ColorInt
  override val todoColor: Int = turqoise

  private companion object {
    val black = Color.parseColor("#312e39")
    val white = Color.parseColor("#bfb8cc")
    val lightRed = Color.parseColor("#943b4e")
    val darkRed = Color.parseColor("#80425d")
    val green = Color.parseColor("#2e997b")
    val lightYellow = Color.parseColor("#BF9C86")
    val darkYellow = Color.parseColor("#94716a")
    val blue = Color.parseColor("#6382bf")
    val turqoise = Color.parseColor("#539ba6")
    val cyan = Color.parseColor("#99BFBA")
    val gutterGrey = Color.parseColor("#4f4b58")
    val commentGrey = Color.parseColor("#736075")
  }
}

