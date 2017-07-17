package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

// https://github.com/orderedlist/github-syntax
class Github: Theme {
  @ColorInt
  override val foregroundColor: Int = foreground
  @ColorInt
  override val backgroundColor: Int = background
  @ColorInt
  override val reservedColor: Int = foreground
  @ColorInt
  override val keywordColor: Int = keyword
  @ColorInt
  override val builtinColor: Int = foreground
  @ColorInt
  override val typeColor: Int = foreground
  @ColorInt
  override val constantColor: Int = constant
  @ColorInt
  override val stringColor: Int = string
  @ColorInt
  override val numberColor: Int = foreground
  @ColorInt
  override val floatColor: Int = foreground
  @ColorInt
  override val booleanColor: Int = foreground
  @ColorInt
  override val specialColor: Int = foreground
  @ColorInt
  override val identifierColor: Int = identifier
  @ColorInt
  override val preprocessorColor: Int = foreground
  @ColorInt
  override val commentColor: Int = comment
  @ColorInt
  override val underlineColor: Int = foreground
  @ColorInt
  override val todoColor: Int = foreground

  companion object {
    val foreground = Color.parseColor("#333333")
    val background = Color.parseColor("#ffffff")
    val comment = Color.parseColor("#969896")
    val constant =  Color.parseColor("#0086b3")
    val keyword = Color.parseColor("#a71d5d")
    val string = Color.parseColor("#183691")
    val identifier = Color.parseColor("#ed6a43")
  }
}

