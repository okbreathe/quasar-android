package com.okbreathe.quasar.components.tagging

class TextReplacementSpan(private val ctx: android.content.Context, private val view: android.view.View) : android.text.style.DynamicDrawableSpan(android.text.style.DynamicDrawableSpan.ALIGN_BOTTOM) {
  private val drawable: android.graphics.drawable.Drawable

  init {
    val spec = android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
    view.measure(spec, spec)
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    drawable = SpanDrawable()
    drawable.setBounds(0, 0, view.measuredWidth, view.measuredHeight)
  }

  override fun getDrawable() = drawable

  fun onClick(view: android.view.View) {
    android.util.Log.d("TextReplacementSpan", "CLICK")
  }

  internal inner class SpanDrawable : android.graphics.drawable.Drawable() {
    override fun draw(canvas: android.graphics.Canvas) {
      canvas.clipRect(bounds)
      view.draw(canvas)
    }
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: android.graphics.ColorFilter?) {}
    override fun getOpacity() = android.graphics.PixelFormat.TRANSLUCENT
  }
}
