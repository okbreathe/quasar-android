package com.okbreathe.quasar.ui.pages

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import com.okbreathe.quasar.R
import com.okbreathe.quasar.components.highlighter.themes.*
import com.okbreathe.quasar.util.isLight
import com.okbreathe.quasar.lib.codeview_android.*
import io.github.kbiakov.codeview.highlight.ColorTheme
import io.github.kbiakov.codeview.highlight.ColorThemeData

open class PageViewModel(protected val ctx: Context) {
  open fun onCreate() {}
  open fun onPause() {}
  open fun onResume() {}
  open fun onDestroy() {}

  val prefs
   get() = PreferenceManager.getDefaultSharedPreferences(ctx)

  val editorTheme: Theme
    get() = when (prefs.getString(ctx.getString(R.string.preference_theme), null)) {
      "SolarizedDark" -> SolarizedDark()
      "SolarizedLight" -> SolarizedLight()
      "Github" -> Github()
      "Synthwave" -> Synthwave()
      "TomorrowNight" -> TomorrowNight()
      "Tomorrow" -> Tomorrow()
      "Monokai" -> Monokai()
      else -> if (prefs.getString(ctx.getString(R.string.preference_ui), "light") == "light")
        Github()
      else
        Monokai()
    }

  // TODO Add missing themes
  val codeTheme: ColorThemeData
    get() = when(prefs.getString(ctx.getString(R.string.preference_theme), null)) {
      "Monokai" -> MONOKAI
      "SolarizedDark" -> SOLARIZED_DARK
      "SolarizedLight" -> SOLARIZED_LIGHT
      "Github" -> GITHUB
      "Synthwave" -> SYNTHWAVE
      "TomorrowNight" -> TOMORROW_NIGHT
      "Tomorrow" -> TOMORROW
      else -> ColorTheme.DEFAULT.theme()
    }

  val showPageExcerpt: Boolean
    get() = prefs.getBoolean(ctx.getString(R.string.preference_page_excerpt), false)

  val editorFontSize: Float
    get() = getFontSize(ctx.getString(R.string.preference_editor_font_size))

  val previewFontSize: Float
    get() = getFontSize(ctx.getString(R.string.preference_preview_font_size))

  // TODO Should be pulling from the theme
  val foreground: Int
    get() = Color.parseColor(if (isLight(ui.accentColor)) "#222222" else "#FFFFFF")

  val ui: UI
    get() = when (prefs.getString(ctx.getString(R.string.preference_ui), "light")) {
      "dark" -> UI(
        primary = R.color.bluegrey_primary,
        secondary = R.color.bluegrey_secondary,
        accent = R.color.bluegrey_accent,
        background = R.color.bluegrey_background
      )
      "editor" -> {
        val theme = editorTheme
        EditorUI(
          primary = theme.foregroundColor,
          secondary = theme.commentColor,
          accent = theme.keywordColor,
          background = theme.backgroundColor
        )
      }
      else -> UI(
        primary = R.color.light_primary,
        secondary = R.color.light_secondary,
        accent = R.color.light_accent,
        background = R.color.light_background
      )
    }

  private fun getFontSize(which: String): Float {
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    // https://stackoverflow.com/questions/24355806/what-is-the-point-of-sharedpreferences-getfloat
    return prefs.getString(which, "14f").toFloat()
  }

  inner open class UI(private val primary: Int, private val secondary: Int, private val accent: Int, private val background: Int) {
    open val primaryColor: Int
      get() = ContextCompat.getColor(ctx, primary)
    open val secondaryColor: Int
      get() = ContextCompat.getColor(ctx, secondary)
    open val accentColor: Int
      get() = ContextCompat.getColor(ctx, accent)
    open val backgroundColor: Int
      get() = ContextCompat.getColor(ctx, background)
  }

  inner class EditorUI(private val primary: Int, private val secondary: Int, private val accent: Int, private val background: Int) :
    UI(primary, secondary, accent, background) {
    override val primaryColor: Int
      get() = primary
    override val secondaryColor: Int
      get() = secondary
    override val accentColor: Int
      get() = accent
    override val backgroundColor: Int
      get() = background
  }
}
