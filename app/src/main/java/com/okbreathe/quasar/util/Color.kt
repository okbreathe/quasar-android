package com.okbreathe.quasar.util

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.widget.EditText
import android.widget.TextView

// https://stackoverflow.com/questions/3942878/how-to-decide-font-color-in-white-or-black-depending-on-background-color
// Should just return bool
fun isLight(r: Int, g: Int, b: Int): Boolean {
  val c = arrayOf(r / 255f, g / 255f, b / 255f)
    .map { if (it <= 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4) }
  return (0.2126 * c[0]  + 0.7152 * c[1] + 0.0722 * c[2]) > 0.179
}

fun isLight(color: Int): Boolean = isLight(Color.red(color), Color.green(color), Color.blue(color))

fun setCursorColors(views: List<EditText>, @ColorInt color: Int) {
  for (view in views) EditTextTint.applyColor(view, color)
}

// https://stackoverflow.com/questions/25996032/how-to-change-programatically-edittext-cursor-color-in-android
// https://stackoverflow.com/questions/29795197/how-to-change-color-of-edittext-handles/29795507
fun setCursorColor(view: EditText, @ColorInt color: Int) {
  try {
    // Get the cursor resource id
    var field = TextView::class.java.getDeclaredField("mCursorDrawableRes").apply { isAccessible = true }
    val drawableResId = field.getInt(view)

    // Get the editor
    field = TextView::class.java.getDeclaredField("mEditor")
    field.isAccessible = true
    val editor = field.get(view)

    // Get the drawable and set a color filter
    val drawable = ContextCompat.getDrawable(view.context, drawableResId)
    drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

    // Set the drawables
    field = editor.javaClass.getDeclaredField("mCursorDrawable")
    field.isAccessible = true
    field.set(editor, arrayOf(drawable, drawable))
    setHandleColor(view, color)
  } catch (ignored: Exception) {
  }
}

fun setHandleColor(view: EditText, @ColorInt color: Int) {
  val field = TextView::class.java.getDeclaredField("mEditor").apply { isAccessible = true }
  val editor = field.get(view)

  val handleLeft = editor.javaClass.getDeclaredField("mSelectHandleLeft").apply{ isAccessible = true }
  val handleRight = editor.javaClass.getDeclaredField("mSelectHandleRight").apply{ isAccessible = true }
  val handleCenter = editor.javaClass.getDeclaredField("mSelectHandleCenter").apply{ isAccessible = true }

  val drawableLeftId = handleLeft.getInt(view)
  val drawableRightId = handleRight.getInt(view)
  val drawableCenterId = handleCenter.getInt(view)

  val drawableLeft = ContextCompat.getDrawable(view.context, drawableLeftId)
  val drawableRight = ContextCompat.getDrawable(view.context, drawableRightId)
  val drawableCenter = ContextCompat.getDrawable(view.context, drawableCenterId)

  drawableLeft.setColorFilter(color, PorterDuff.Mode.SRC_IN)
  drawableRight.setColorFilter(color, PorterDuff.Mode.SRC_IN)
  drawableCenter.setColorFilter(color, PorterDuff.Mode.SRC_IN)

  handleLeft.set(editor, arrayOf(drawableLeft, drawableLeft))
  handleRight.set(editor, arrayOf(drawableRight, drawableRight))
  handleCenter.set(editor, arrayOf(drawableCenter, drawableCenter))
}
