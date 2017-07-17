package com.okbreathe.quasar.components.highlighter.themes

import android.support.annotation.ColorInt

// TODO This is just a copy of github
class SolarizedLight: SolarizedDark() {
  @ColorInt
  override val foregroundColor: Int = base01
  @ColorInt
  override val backgroundColor: Int = base3
}
