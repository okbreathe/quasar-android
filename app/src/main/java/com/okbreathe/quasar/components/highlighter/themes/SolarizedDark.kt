package com.okbreathe.quasar.components.highlighter.themes

import android.graphics.Color
import android.support.annotation.ColorInt

open class SolarizedDark: Theme {
  // exe "hi! normal"         .s:fmt_none   .s:fg_base0  .s:bg_back
  // exe "hi! comment"        .s:fmt_ital   .s:fg_base01 .s:bg_noneclass solarizeddark: theme {
  // exe "hi! constant"       .s:fmt_none   .s:fg_cyan   .s:bg_none  override val foregroundcolor: int = base0
  // exe "hi! identifier"     .s:fmt_none   .s:fg_blue   .s:bg_none  override val backgroundcolor: int = base02
  // exe "hi! statement"      .s:fmt_none   .s:fg_green  .s:bg_none
  // exe "hi! preproc"        .s:fmt_none   .s:fg_orange .s:bg_none  override val reservedcolor: int = green
  // exe "hi! type"           .s:fmt_none   .s:fg_yellow .s:bg_none  override val keywordcolor: int = reservedcolor
  // exe "hi! special"        .s:fmt_none   .s:fg_red    .s:bg_none  override val builtincolor: int = pink
  // exe "hi! underlined"     .s:fmt_none   .s:fg_violet .s:bg_none  override val typecolor: int = blue
  // exe "hi! ignore"         .s:fmt_none   .s:fg_none   .s:bg_none
  // exe "hi! error"          .s:fmt_bold   .s:fg_red    .s:bg_none  override val constantcolor: int = purple
  // exe "hi! todo"           .s:fmt_bold   .s:fg_magenta.s:bg_none  override val stringcolor: int = yellow
  @ColorInt
  override val foregroundColor: Int = base0
  @ColorInt
  override val backgroundColor: Int = base02

  @ColorInt
  override val reservedColor: Int = green
  @ColorInt
  override val keywordColor: Int = blue
  @ColorInt
  override val builtinColor: Int = yellow
  @ColorInt
  override val typeColor: Int = yellow

  @ColorInt
  override val constantColor: Int = cyan
  @ColorInt
  override val stringColor: Int = constantColor
  @ColorInt
  override val numberColor: Int = constantColor
  @ColorInt
  override val floatColor: Int = constantColor
  @ColorInt
  override val booleanColor: Int = constantColor

  @ColorInt
  override val specialColor: Int = red
  @ColorInt
  override val identifierColor: Int = blue
  @ColorInt
  override val preprocessorColor: Int = orange
  @ColorInt
  override val commentColor: Int = base01
  @ColorInt
  override val underlineColor: Int = violet
  @ColorInt
  override val todoColor: Int = magenta

  protected companion object {
    val base03 = Color.parseColor("#002b36")
    val base02 = Color.parseColor("#073642")
    val base01 = Color.parseColor("#586e75")
    val base00 = Color.parseColor("#657b83")
    val base0 = Color.parseColor("#839496")
    val base1 = Color.parseColor("#93a1a1")
    val base2 = Color.parseColor("#eee8d5")
    val base3 = Color.parseColor("#fdf6e3")
    val yellow = Color.parseColor("#B58900")
    val orange = Color.parseColor("#CB4B16")
    val red = Color.parseColor("#DC322F")
    val magenta = Color.parseColor("#D33682")
    val violet = Color.parseColor("#6C71C4")
    val blue = Color.parseColor("#268BD2")
    val cyan = Color.parseColor("#2AA198")
    val green = Color.parseColor("#859900")
  }
}
